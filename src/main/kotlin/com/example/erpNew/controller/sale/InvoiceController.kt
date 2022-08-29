package com.example.erpNew.controller.sale

import com.example.erpNew.base.GenericRestfulController
import com.example.erpNew.model.sale.Invoice
import com.example.erpNew.responseFormat.response.ResponseDTO
import com.example.erpNew.service.sale.InvoiceServiceImp
import com.example.erpNew.service.stock.StockTransactionServiceImp
import com.example.erpNew.utilities.AppConstant
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/invoice")
class InvoiceController : GenericRestfulController<Invoice>(Invoice::class.java){

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
                it.item!!, it.qty!!, ref = entity.invoiceNo
            )
        }
    }

    @GetMapping(AppConstant.LIST_DTO_PATH)
    @ApiImplicitParams(value = [
        ApiImplicitParam(name = "customerId", required = false, paramType = "query"),
        ApiImplicitParam(name = "customerName",required = false, paramType = "query")
    ])
    fun listInvoiceDTO (@ApiParam(hidden = true) @RequestParam allParams: MutableMap<String, String>): ResponseDTO {
        val rs = invoiceService.findAllList(allParams)
        return JSONFormat.respondPage(rs)
    }
}