package com.example.erpNew.model.model_sale

import com.example.erpNew.base.BaseEntity
import com.example.erpNew.utilities.AppConstant
import java.util.*
import javax.persistence.*

@Entity
data class SaleInvoice(

    var series: String? = null,
    var customStatus: String? = AppConstant.DRAFT,

    var additionalDisPer: Float? = 0f,
    var additionalDisAmount: Double? = 0.0,

    var dueDate: Date? = null,
    var vatAmount: Double? = 0.0,
    var vatPer: Float? = 0f,
    var isIncludeVat: Boolean? = false,

    var totalQty: Float? = 0f,
    var totalCost: Double? = 0.0,
    var grandTotal: Float? = 0f,
    var total: Double? = 0.0,
    var unpaidAmount: Float? = 0f,

    var invoicePer: Float? = 0f, //remove
    var invoiceAmount: Double? = 0.0,//remove


    var warehouseAddress: String? = null,
    var shippingAddress: String? = null,


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id", nullable = true)
    var saleOrder: SaleOrder? = null,


    var customer: String? = null,


    var warehouse: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_note_id", nullable = true)
    var deliveryNote: DeliveryNote? = null,

    @OneToMany(mappedBy = "saleInvoice", cascade = [CascadeType.ALL])
    var saleInvoiceDetail: MutableList<SaleInvoiceDetail>? = null


) : BaseEntity()
