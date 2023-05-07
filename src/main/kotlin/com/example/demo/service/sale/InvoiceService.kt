package com.example.demo.service.sale

import com.example.demo.model.sale.dto.InvoiceDTO
import org.springframework.data.domain.Page

interface InvoiceService {

    fun testThrow(i:Long)
    fun findAllList(allParams: Map<String, String>?): Page<InvoiceDTO>
}