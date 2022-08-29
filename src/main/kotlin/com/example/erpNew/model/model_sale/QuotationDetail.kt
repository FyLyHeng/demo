package com.example.erpNew.model.model_sale

import com.example.erpNew.base.BaseEntity
import com.example.erpNew.model.item.Item
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*

@Entity
data class QuotationDetail(

        var deliveryDate: Date? = null,
        var deliveryFee: Double? = 0.0,

        var qty: Float? = 0f,
        var cost: Double? = 0.0,
        var rate: Double? = 0.0,

        var conversionFactor: Double? = null,
        var stockQty: Double? = 0.0, // stockQty = conversionFactor * qty

        var discountPercent: Float? = 0f,
        var discount: Float? = null,
        var amount: Double? = 0.0,


        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "item_id", nullable = true)
        var item: Item? = null,


        @JsonIgnore
        @ManyToOne(targetEntity = Quotation::class)
        @JoinColumn(name = "quotation_id", insertable = true, updatable = false,nullable = true)
        var quotation: Quotation? = null

) : BaseEntity()

