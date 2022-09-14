package com.example.demo.controller.sale

import com.example.demo.core.GenericRestfulController
import com.example.demo.core.responseFormat.exception.entityExecption.BadRequestException
import com.example.demo.core.responseFormat.exception.entityExecption.NotFoundException
import com.example.demo.model.sale.Invoice
import com.example.demo.core.responseFormat.response.ResponseDTO
import com.example.demo.service.sale.InvoiceServiceImp
import com.example.demo.service.stock.StockTransactionServiceImp
import com.example.demo.utilities.AppConstant
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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

    override fun beforeSave(entity: Invoice) {
        try {
            super.beforeSave(entity)
        }catch (e : BadRequestException){
            throw e
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



    @GetMapping("/throw")
    fun testThrow(@RequestParam i : Long): ResponseDTO {

        if (i == 10L) {
            invoiceService.testThrow(10)
        }
        else if (i == 20L){
            throw RuntimeException("I can't not be negative")
        }

        else if (i == 30L){
            throw NotFoundException("custom not found exp")
        }

        return JSONFormat.respondDynamic("OK",  HttpStatus.OK, HttpStatus.OK.reasonPhrase , 0 )
    }
}