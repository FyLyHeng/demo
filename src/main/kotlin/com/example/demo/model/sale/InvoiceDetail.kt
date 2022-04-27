package com.example.demo.model.sale

import com.example.demo.model.item.Item
import com.fasterxml.jackson.annotation.JsonIgnore
import com.ig.erp.base.BaseEntity
import javax.persistence.*


@Entity
data class InvoiceDetail(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Long?,
    var qty:Int?=0,
    var price:Double?=0.0,

    @OneToOne(fetch = FetchType.LAZY)
    var item: Item?,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", insertable = true, updatable = false,nullable = true)
    var invoice : Invoice?= null


):BaseEntity()
