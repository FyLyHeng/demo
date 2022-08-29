package com.example.erpNew.model.model_sale


import com.example.erpNew.base.BaseEntity
import com.example.erpNew.model.item.Item
import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
data class DeliveryNoteDetail(


        var qty: Float? = 0f,
        var rate: Double? = 0.0,
        var stockQty: Float? = 0f,
        var conversionFactor: Float? = 0f,

        var discountPercent: Float? = 0f,
        var discount: Float? = 0f,
        var amount: Double? = 0.0,

        @Column(columnDefinition="TEXT")
        var serialNo : String? = null,
        var HasSerialNo: Boolean = false,

        @OneToOne
        @JoinColumn(name = "item_id")
        var item: Item? = null,


        @JsonIgnore
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "delivery_note_id", insertable = true, updatable = false,nullable = true)
        var deliveryNote : DeliveryNote?= null


): BaseEntity()
