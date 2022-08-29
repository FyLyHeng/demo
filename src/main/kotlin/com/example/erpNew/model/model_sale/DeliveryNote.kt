package com.example.erpNew.model.model_sale

import com.example.erpNew.base.BaseEntity
import com.example.erpNew.utilities.AppConstant
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*

@Entity
class DeliveryNote(

    var series: String? = null,
    var note: String? = null,

    var priority: Int? = 0,
    var immediateTransfer: Boolean? = false,
    var dateDone: Date? = null,
    var vatPer: Float? = 0f,
    var vatAmount: Double? = 0.0,


    @Column(length = 1000)
        var shippingAddress: String? = null,
    var shippingTitle: String? = null,
    var shippingAddressId: Int? = null,


    @Column(length = 1000)
        var billingAddress: String? = null,
    var billingTitle: String? = null,
    var billingAddressId: Int? = null,

    var warehouseAddress: String? = null,
    var totalQty: Int? = 0,
    var additionalDisPer: Float? = 0f,
    var additionalDisAmount: Double? = 0.0,
    var grandTotal: Double? = 0.0,
    var total: Double? = 0.0,
    var customStatus: String? = AppConstant.DRAFT,


    var customer: String? = null,


    var warehouse: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "delivery_type_id")
        var deliveryType: DeliveryType? = null,


    @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "driver_id")
        var driver: Driver? = null,


    @JsonIgnore
        @OneToMany(mappedBy = "deliveryNote", cascade = [CascadeType.ALL])
        var saleInvoice: MutableList<SaleInvoice>? = null,


    @OneToMany(targetEntity = DeliveryNoteDetail::class, cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
        @JoinColumn(name = "delivery_note_id",referencedColumnName = "id")
        var deliveryNoteDetail: MutableList<DeliveryNoteDetail>?=null,

    @ManyToOne
        var saleOrder: SaleOrder? = null

): BaseEntity() {}
