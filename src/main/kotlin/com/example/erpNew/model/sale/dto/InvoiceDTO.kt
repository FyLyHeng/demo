package com.example.erpNew.model.sale.dto


interface InvoiceDTO {
    var invoiceNo:String?
    var customerName:String?
    var grandTotal:Double?
    var invoiceDetail : MutableList<InvoiceDetailDTO>?

}

interface InvoiceDetailDTO {
    var price: Double?
    var item : ItemDTO?
}

interface ItemDTO {
    var name :String ?
}