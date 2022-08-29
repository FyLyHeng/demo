package com.example.erpNew.service.saleImp

import com.example.erpNew.base.BaseService
import com.example.erpNew.model.model_sale.SaleOrder
import com.example.erpNew.service.saleSupport.ISaleOrderService
import org.springframework.stereotype.Service

@Service
class SaleOrderService : ISaleOrderService, BaseService<SaleOrder>() {

    override fun getSaleOrderByCustomer(customerName: String): MutableList<SaleOrder> {

        val allParams: MutableMap<String, String> = mutableMapOf(
            "orderBy" to "customer",
            "sortBy" to "DESC"
        )
        val rs = findAll(allParams){ predicates, cb, root -> customerName.let { predicates.add(cb.equal(root.get<String>("customer"), it)) } }
        return rs as MutableList<SaleOrder>
    }



    override fun updateToCancel(id: Long) {
        updateStatus(id, "Cancel"){

        }

    }

    override fun updateToComplete(id: Long) {
        TODO("Not yet implemented")
    }

    override fun updateToDraft(id: Long) {
        TODO("Not yet implemented")
    }

    override fun updateToSubmit(id: Long) {
        TODO("Not yet implemented")
    }
}