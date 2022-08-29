package com.ig.erp.service.implement.sale

import com.ig.erp.responseFormat.exception.CustomNotAcceptableException
import com.ig.erp.responseFormat.exception.CustomNotFoundException
import com.ig.erp.model.model_sale.DeliveryNote
import com.ig.erp.model.model_sale.SaleOrderDetail
import com.ig.erp.responseFormat.response.ResponseObject
import com.ig.erp.model.response.UomDropDown
import com.ig.erp.repository.sale.SaleOrderDetailRepository
import com.ig.erp.service.services.SaleOrderDetailService
import com.ig.erp.service.services.UomService
import com.ig.erp.utils.AppConstant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class SaleOrderDetailServiceImpl : SaleOrderDetailService {

    @Autowired
    lateinit var saleOrderDetailRepository: SaleOrderDetailRepository
    @Autowired
    lateinit var uomService: UomService

    override fun findAllList(q: String?, page: Int, size: Int): Page<SaleOrderDetail>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findById(id: Long): SaleOrderDetail? {
        return saleOrderDetailRepository.findByIdAndStatusTrue(id)
                ?: throw CustomNotFoundException("sale order detail id $id not exist")
    }

    override fun addNew(t: SaleOrderDetail): SaleOrderDetail? {
        t.amount= t.qty?.times(t.rate!!)
        return saleOrderDetailRepository.save(t)
    }

    override fun addAll(saleOrderDetails: MutableList<SaleOrderDetail>): MutableList<SaleOrderDetail>? {
        return saleOrderDetailRepository.saveAll(saleOrderDetails)
    }

    override fun updateObj(id: Long, t: SaleOrderDetail): SaleOrderDetail? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAll(): List<SaleOrderDetail>? {
        TODO("n ot implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findBySaleOrderId(saleOrderId: Long): MutableList<SaleOrderDetail>? {
        return saleOrderDetailRepository.findBySaleOrderId(saleOrderId)
                ?: throw CustomNotFoundException("sale order detail cannot found saleOrderId $saleOrderId.")
    }

    override fun getUomDropDown(id: Long): UomDropDown? {
        return UomDropDown(uomService.findAll())
    }

    override fun updateDeliveredQty(deliveryNote: DeliveryNote): ResponseObject? {
        val saleOrderId = deliveryNote.saleOrder?.id
        deliveryNote.deliveryNoteDetail?.map {
            val saleOrderDetail = saleOrderDetailRepository.findBySaleOrderIdAndItemId(saleOrderId!!, it.item?.id!!)!!

            val deliveryQty = it.qty?.toInt()
            val remainQty = saleOrderDetail.remainQty?.toInt()

            val itemName = saleOrderDetail.item?.itemName

            if (deliveryQty!! <= remainQty!!) {
                // when submit update remain qty by minus delivery qty
                saleOrderDetail.remainQty = saleOrderDetail.remainQty?.minus(it.qty!!)
                saleOrderDetail.deliveryQty = saleOrderDetail.deliveryQty!! + it.qty!!
                saleOrderDetailRepository.save(saleOrderDetail)
            }
            else throw CustomNotAcceptableException("[$itemName] delivery qty ($deliveryQty) must be equal or smaller than sale order qty ($remainQty)")
        }
        return ResponseObject(200, AppConstant.SUCCESS)
    }
}
