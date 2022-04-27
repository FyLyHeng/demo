package com.example.demo.controller.sale

import com.example.demo.base.GenericRestfulController
import com.example.demo.model.sale.Invoice
import com.example.demo.service.sale.InvoiceServiceImp
import com.example.demo.service.stock.StockTransactionServiceImp
import com.example.demo.utilities.AppConstant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/invoice")
class InvoiceController : GenericRestfulController<Invoice>(){

    @Autowired
    lateinit var stockTransactionService: StockTransactionServiceImp
    @Autowired
    lateinit var invoiceService: InvoiceServiceImp

    override fun update(id: Long, entity: Invoice): Invoice? {
        return update(id, entity, exclude = listOf("invoiceDetail", "invoiceNo")){
            it.invoiceDetail?.clear()
            it.invoiceDetail?.addAll(entity.invoiceDetail!!)
        }
    }

    override fun create(entity: Invoice): Invoice? {
        return create(entity){
            it.invoiceNo = documentSettingService.getNextSeries("invoice")
        }
    }

    override fun afterSaved(entity: Invoice) {
        entity.invoiceDetail?.forEach {
            stockTransactionService.recordStockTransaction(
                it.item!!, it.qty!!, Date(), entity.invoiceNo
            )
        }
    }

    @GetMapping(AppConstant.LIST_DTO_PATH)
    fun listInvoiceDTO (@RequestParam allParams: MutableMap<String, String>): MutableMap<String, Any> {
        val rs = invoiceService.findAllList(allParams)
        return response.responseObject(rs.content, rs.totalElements)
    }
}