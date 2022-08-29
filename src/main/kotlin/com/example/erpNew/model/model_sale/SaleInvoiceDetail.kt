package com.example.erpNew.model.model_sale

import com.example.erpNew.base.BaseEntity
import com.example.erpNew.model.item.Item
import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
data class SaleInvoiceDetail(

        var qty: Int? = 0,
        var rate: Double? = 0.0,
        var cost: Double? = 0.0,
        var conversionFactor: Float? = 0f,

        var discountPercent: Float? = 0f,
        var discount: Float? = 0f,
        var amount: Double? = 0.0,


        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "item_id", nullable = true)
        var item: Item? = null,


        @JsonIgnore
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "sale_invoice_id", nullable = true)
        var saleInvoice : SaleInvoice?= null
) : BaseEntity()
