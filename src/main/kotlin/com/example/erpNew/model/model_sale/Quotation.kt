package com.example.erpNew.model.model_sale

import com.example.erpNew.base.BaseEntity
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import javax.persistence.*


@Entity
data class Quotation(

        var series: String? = null,
        var customStatus: String? = null,

        var postDate: Date? = null,
        var estimateDeliveryDate: Date? = null,
        var expiredDate: Date? = null,

        var isIncludeVat:Boolean ?=false,
        var vatAmount: Float? = 0f,
        var vatPer: Float? = 0f,
        var totalQty: Int? = 0,

        var cusPurchaseOrder: Int? = 0,
        var additionalNote: String? = null,
        var additionalDisPer: Float? = 0f,
        var additionalDisAmount: Float? = 0f,
        var grandTotal: Float? = 0f,
        var totalCost: Float? = 0f,

        var total: Double? = 0.0,
        var totalTaxCharge: Float? = 0f,

        var shippingAddress: String? = null,
        var shippingTitle: String? = null,
        var shippingAddressId: Int? = null,

        var billingAddress: String? = null,
        var billingTitle: String? = null,
        var billingAddressId: Int? = null,


//        @OneToOne(fetch = FetchType.LAZY)
//        @JoinColumn(name = "price_list_id")
//        var priceList: PriceList? = null,
//
//        @OneToOne(fetch = FetchType.LAZY)
//        @JoinColumn(name = "sale_person_id")
//        var salePerson: SalePerson? = null,


        @JsonIgnore
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "sale_order_id")
        var saleOrder: SaleOrder? = null,


        var customer: String? = null,


        var currency: String? = null,


        var warehouse: String? = null,


        var termCondition: String? = null,


        @OneToMany(targetEntity = QuotationDetail::class, cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
        @JoinColumn(name = "quotation_id",referencedColumnName = "id")
        var quotationDetail: MutableList<QuotationDetail>? = null

) : BaseEntity()
