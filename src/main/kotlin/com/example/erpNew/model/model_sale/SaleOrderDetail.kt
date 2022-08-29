package com.example.erpNew.model.model_sale

import com.example.erpNew.base.BaseEntity
import com.example.erpNew.model.item.Item
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*

@Entity
data class  SaleOrderDetail (

        var deliveryDate: Date?=null,
        var deliveryQty: Float? = 0f,

        var amount : Double? = 0.0,
        var conversionFactor: Float? = 0f,

        var qty : Float = 0f,
        var remainQty: Float? = 0f,
        var stockQty: Float? = 0f,

        var discountPercent : Float? = 0f,
        var discount : Float? = null,

        var rate : Double = 0.0,
        var valuationRate : Double? = 0.0,

        var cost: Double?=0.0,
        var totalCost: Double?=0.0,

        @Column(columnDefinition="TEXT")
        var serialNo : String? = null,
        var hasSerialNo: Boolean? = false,


        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "item_id")
        var item : Item? = null,

        @JsonIgnore
        @ManyToOne
        @JoinColumn(name = "sale_order_id", insertable = true, updatable = false,nullable = true)
        var saleOrder : SaleOrder? = null
) : BaseEntity()
