package com.example.erpNew.model.model_sale

import com.example.erpNew.base.BaseEntity
import com.example.erpNew.utilities.AppConstant
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*

@Entity
data class SaleOrder(

    var series: String? = null,
    var customStatus: String? = AppConstant.DRAFT,

    var postDate: Date? = null,
    var estimateDeliveryDate: Date? = null,

    var isIncludeVat: Boolean? = false,
    var vatPer: Float? = 0f,
    var vatAmount: Double? = 0.0,
    var isDeposit: Boolean? = false,


    var cusPurchaseOrder: Int? = 0,
    var additionalNote: String? = null,
    var totalQty: Int? = 0,
    var additionalDisPer: Float? = 0f,
    var additionalDisAmount: Float? = 0f,
    var grandTotal: Float? = 0f,
    var total: Double? = 0.0,
    var totalCost: Double? = 0.0,
    var totalTaxCharge: Float? = 0f,


    @Column(length = 1000)
    var shippingAddress: String? = null,
    var shippingTitle: String? = null,
    var shippingAddressId: Int? = null,

    @Column(length = 1000)
    var billingAddress: String? = null,
    var billingTitle: String? = null,
    var billingAddressId: Int? = null,

    var billedStatus: String? = null,
    var perBilled: Float? = 0f,

    var deliveryStatus: String? = null,
    var perDelivered: Float? = 0f,


    var prepaymentAmount: Double? = 0.0,
    var prepaymentBalance: Double? = 0.0,


    var customer: String? = null,



    var currency: String? = null,


    var warehouse: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id")
    var quotation: Quotation? = null,


    @JsonIgnore
    @OneToMany(mappedBy = "saleOrder", cascade = [CascadeType.ALL])
    var saleInvoice: MutableList<SaleInvoice>? = null,

    @JsonIgnore
    @OneToMany(mappedBy = "saleOrder", cascade = [CascadeType.ALL])
    var deliveryNote: MutableList<DeliveryNote>? = null,

    @OneToMany(
        targetEntity = SaleOrderDetail::class,
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    @JoinColumn(name = "sale_order_id", referencedColumnName = "id")
    var saleOrderDetail: MutableList<SaleOrderDetail>? = null,

    ) : BaseEntity()
