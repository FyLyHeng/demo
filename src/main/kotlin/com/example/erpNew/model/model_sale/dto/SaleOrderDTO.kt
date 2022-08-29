package com.ig.erp.model.model_sale.dto

import java.util.*

interface SaleOrderDTO {
    var id: Long
    var series: String
    var customStatus: String
    var postDate : Date?
    var estimateDeliveryDate: Date?
    var totalQty: Int?
    var grandTotal: Float?

    var billedStatus: String
    var perBilled: Float?

    var deliveryStatus: String?
    var perDelivered: Float?

    var customer : Customer?
}

interface Customer {
    var id:Long?
    var name:String?
}