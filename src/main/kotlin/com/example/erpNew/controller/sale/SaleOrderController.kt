package com.example.erpNew.controller.sale

import com.example.erpNew.base.GenericRestfulController
import com.example.erpNew.model.model_sale.SaleOrder
import com.example.erpNew.service.saleSupport.ISaleOrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page

class SaleOrderController : GenericRestfulController<SaleOrder>(SaleOrder::class.java) {

    @Autowired
    lateinit var saleOrderService : ISaleOrderService


    override fun create(entity: SaleOrder): SaleOrder {
        return saleOrderService.addNew(entity)
    }

    override fun listCriteria(allParams: Map<String, String>): Page<SaleOrder>? {
        return saleOrderService.listCriteria(allParams)
    }
}