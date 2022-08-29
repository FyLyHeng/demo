package com.example.erpNew.service.saleSupport

import com.example.erpNew.model.model_sale.SaleOrder
import com.example.erpNew.base.IBaseService
import com.example.erpNew.service.status.IStatusService
import org.springframework.stereotype.Component

@Component
interface ISaleOrderService : IBaseService<SaleOrder>, IStatusService {
    fun getSaleOrderByCustomer(customerName:String) : MutableList<SaleOrder>
}


interface ISaleOrderDeliveryService {}
interface ISaleOrderInvoiceService {}
interface ISaleOrderValidationService {}

