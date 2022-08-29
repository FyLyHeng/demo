package com.example.erpNew.controller.sale

import com.example.erpNew.model.sale.Invoice
import com.example.erpNew.model.sale.dto.InvoiceDTO
import com.example.erpNew.responseFormat.exception.generalException.NotAcceptableException
import com.example.erpNew.service.sale.InvoiceServiceImp
import com.example.erpNew.service.stock.StockTransactionServiceImp
import com.example.erpNew.utilities.AppConstant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/test")
class TestController {

    @Autowired
    lateinit var stockTransactionService: StockTransactionServiceImp
    @Autowired
    lateinit var invoiceService: InvoiceServiceImp


    @PostMapping
    fun post (@RequestBody invoice: Invoice): ResponseEntity<Invoice> {

        if (invoice.customerName.length>1){
            throw NotAcceptableException("Customer name are too long")
        }

        return ResponseEntity(invoiceService.invoiceRepository.save(invoice), HttpStatus.CREATED)
    }


    @GetMapping(AppConstant.LIST_DTO_PATH)
    fun listInvoiceDTO (@RequestParam allParams: MutableMap<String, String>): ResponseEntity<Page<InvoiceDTO>> {
        val rs = invoiceService.findAllList(allParams)
        return ResponseEntity(rs, HttpStatus.OK)
    }

    @DeleteMapping("/delete/{id}")
    fun delete (@PathVariable id :Long){
        invoiceService.invoiceRepository.deleteById(id)
    }
}