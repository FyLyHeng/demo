package com.example.demo.repository.sale

import com.example.demo.model.sale.Invoice
import com.example.demo.core.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface InvoiceRepository: BaseRepository<Invoice>