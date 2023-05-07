package com.example.demo.model.sale

import com.example.demo.core.BaseEntity
import java.util.Date
import javax.persistence.*

@Entity
data class Invoice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Long?,
    var invoiceNo:String?=null,
    var date: Date?,
    var customerName:String?,
    var subTotal:Double?,
    var grandTotal:Double=0.0,
    var vatAmount:Double?,


    @OneToMany(
        targetEntity = InvoiceDetail::class, cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY, orphanRemoval = true
    )
    @JoinColumn(name = "invoice_id",referencedColumnName = "id")
    var invoiceDetail: MutableList<InvoiceDetail>?=null,


):BaseEntity()
