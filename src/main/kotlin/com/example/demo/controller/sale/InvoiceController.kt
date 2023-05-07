package com.example.demo.controller.sale

import com.example.demo.core.GenericRestfulController
import com.example.demo.core.Slf4k
import com.example.demo.core.Slf4k.Companion.log
import com.example.demo.core.responseFormat.exception.entityExecption.NotFoundException
import com.example.demo.core.responseFormat.exception.generalException.NotAcceptableException
import com.example.demo.core.responseFormat.response.ResponseDTO
import com.example.demo.model.sale.Invoice
import com.example.demo.model.sale.dto.InvoiceDTO
import com.example.demo.service.sale.InvoiceService
import com.example.demo.service.stock.StockTransactionServiceImp
import com.example.demo.utilities.AppConstant
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@Slf4k
@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/invoice")
class InvoiceController : GenericRestfulController<Invoice>(allowUpdate = false, allowDelete = false){

    @Autowired
    lateinit var stockTransactionService: StockTransactionServiceImp

    @Autowired
    lateinit var invoiceService: InvoiceService



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


    @PutMapping("{id}")
    override fun update(@PathVariable(value = "id") id: Long, @RequestBody entity: Invoice?): ResponseDTO {

        val data = update(id, entity!!, fieldsProtected = listOf("invoiceNo","invoiceDetail")) {
            it.invoiceDetail?.clear()
            it.invoiceDetail?.addAll(entity.invoiceDetail?: listOf())
        }

        return JSONFormat.respondObj(data)
    }


    override fun create(entity: Invoice, customFields: (targetOBJ: Invoice) -> Unit): Invoice {

        val data = super.create(entity) { invoice->
            invoice.invoiceNo = "inv-000001"
            invoice.date = Date()
        }
        return data
    }


    override fun beforeSave(entity: Invoice) {
        if (entity.customerName == null){
            throw NotAcceptableException("Customer name Can Not Be Null")
        }
    }

    override fun afterSaved(entity: Invoice) {
        entity.invoiceDetail?.forEach {
            stockTransactionService.recordStockTransaction(
                it.item!!, it.qty!!, ref = entity.invoiceNo
            )
        }
    }


    override fun list(@RequestParam allParams: Map<String, String>): ResponseDTO {
        log.info("params _${allParams.entries}")
        return super.list(allParams)
    }


//    @GetMapping(AppConstant.LIST_DTO_PATH)
//    @ApiImplicitParams(value = [
//        ApiImplicitParam(name = "customerId", required = false, paramType = "query"),
//        ApiImplicitParam(name = "customerName",required = false, paramType = "query")
//    ])
//    override fun <R : Any> listCriteriaWithProjection(@RequestParam allParams: Map<String, String>, customDto: Class<R>): ResponseDTO {
//
//        val data = super.listCriteriaWithProjection(allParams, InvoiceDTO::class.java){
//            predicates: ArrayList<Predicate>, cb: CriteriaBuilder, root: Root<Invoice> ->
//
//        }
//
//        return JSONFormat.respondPage(data)
//    }

    @GetMapping(AppConstant.LIST_DTO_PATH)
    @ApiImplicitParams(value = [
        ApiImplicitParam(name = "customerId", required = false, paramType = "query"),
        ApiImplicitParam(name = "customerName",required = false, paramType = "query")
    ])
    override fun <R : Any> listCriteriaWithProjection( @RequestParam allParams: Map<String, String>, customDto: Class<R>): ResponseDTO {
        return super.listCriteriaWithProjection(allParams, InvoiceDTO::class.java)
    }
}