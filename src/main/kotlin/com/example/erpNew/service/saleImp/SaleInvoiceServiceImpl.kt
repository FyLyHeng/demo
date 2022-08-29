package com.ig.erp.service.implement.sale

import com.example.erpNew.service.saleImp.PostGLSaleService
import com.ig.erp.responseFormat.exception.CustomNotAcceptableException
import com.ig.erp.responseFormat.exception.CustomNotFoundException
import com.ig.erp.model.enum.GlobalSearchDocType
import com.ig.erp.model.model_sale.SaleInvoice
import com.ig.erp.responseFormat.response.ResponseObject
import com.ig.erp.repository.sale.SaleInvoiceRepository
import com.ig.erp.service.implement.system.DocumentSettingServiceImpl
import com.ig.erp.service.implement.system.GlobalSearchServiceImpl
import com.ig.erp.service.services.SaleInvoiceService
import com.ig.erp.service.services.SaleOrderService
import com.ig.erp.utils.AppConstant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*
import javax.persistence.criteria.Predicate
import javax.transaction.Transactional

@Service
class SaleInvoiceServiceImpl : SaleInvoiceService {

    @Autowired
    lateinit var saleInvoiceRepository: SaleInvoiceRepository
    @Autowired
    lateinit var documentSettingService: DocumentSettingServiceImpl
    @Autowired
    lateinit var postGLSaleService: PostGLSaleService
    @Autowired
    lateinit var globalSearchServiceImpl: GlobalSearchServiceImpl
    @Autowired
    lateinit var saleOrderService : SaleOrderService

    override fun findAllList(q: String?, page: Int, size: Int): Page<SaleInvoice>? {
        return saleInvoiceRepository.findAll({ root, query, cb ->
            val predicates = ArrayList<Predicate>()
            if (q != null) {
                if (q.matches("-?\\d+(\\.\\d+)?".toRegex())) {
                    val customerId = cb.equal(root.get<String>("customer").get<Long>("id"), q)
                    predicates.add(customerId)
                } else {
                    val series = cb.like(root.get("series"), "$q%")
                    predicates.add(cb.or(series))
                }
            }
            predicates.add(cb.isTrue(root.get<Boolean>("status")))
            query.orderBy(cb.asc(root.get<String>("id")))
            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }

    override fun findFullCriteria(customerId: String?, saleOrderSeries: String?, saleInvoiceSeries: String?, page: Int, size: Int): Page<SaleInvoice>? {
        return saleInvoiceRepository.findAll({ root, query, cb ->
            val predicates = ArrayList<Predicate>()
            if (customerId != null) {
                val cusId = cb.equal(root.get<String>("customer").get<Long>("id"), customerId)
                predicates.add(cusId)
            }
            if (saleOrderSeries != null) {
                val saleSeries = cb.like(root.get<String>("saleOrder").get("series"), "%${saleOrderSeries.trim().toUpperCase()}%")
                predicates.add(saleSeries)
            }
            if (saleInvoiceSeries != null) {
                val invoiceSeries = cb.like(root.get("series"), "%${saleInvoiceSeries.trim().toUpperCase()}%")
                predicates.add(invoiceSeries)
            }
            predicates.add(cb.isTrue(root.get<Boolean>("status")))
            query.orderBy(cb.asc(root.get<String>("id")))
            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }

    override fun findById(id: Long): SaleInvoice? {
        return saleInvoiceRepository.findByIdAndStatusTrue(id)
                ?: throw CustomNotFoundException("Invoice id $id doesn't exist")
    }

    /**
     * - add new sale invoice
     * - update sale's first process flow to 'SaleInvoice'
     * - update sale's billed status to partly or fully
     * - update sale's custom status to 'COMPLETE' or 'TO_DELIVERY'
     */
    @Transactional
    override fun addNew(saleInvoice: SaleInvoice): SaleInvoice? {
        checkInvoiceExceptions(saleInvoice)

        //TODO change condition check
        val currentIssueInvoicePer = saleInvoiceRepository.sumInvoicePerBySaleOrderId(saleInvoice.saleOrder?.id!!)
        when  {
            currentIssueInvoicePer == null -> throw CustomNotAcceptableException("The given id doesn't exist!")
            currentIssueInvoicePer == 100f -> throw CustomNotAcceptableException("You already complete Invoice")
            currentIssueInvoicePer + saleInvoice.invoicePer!! > 100f -> {
                throw CustomNotAcceptableException("Should input Percentage or Amount in Sale invoice properly, $currentIssueInvoicePer% in progress!")
            }
        }

        saleInvoice.series = documentSettingService.getNextSeries("saleInvoice", saleInvoice.isIncludeVat!!)
        saleInvoice.unpaidAmount = saleInvoice.grandTotal
        saleInvoice.saleInvoiceDetail?.forEach {
            it.saleInvoice = saleInvoice
        }


        val t = saleInvoiceRepository.save(saleInvoice)
        globalSearchServiceImpl.addGlobalSearch(GlobalSearchDocType.SaleInvoice, t.series, t.id)
        return t
    }

    override fun updateObj(id: Long, t: SaleInvoice): SaleInvoice? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateStatusCompleted(id: Long): ResponseObject? {
        val invoice = findById(id)
        invoice?.customStatus = AppConstant.COMPLETED
        saleInvoiceRepository.save(invoice!!)

        postGLSaleService.postSaleInvoice(invoice)
        return ResponseObject(200, AppConstant.SUCCESS)
    }

    override fun updateStatusUnpaid(id: Long): ResponseObject? {
        val invoice = findById(id)!!

        invoice.customStatus = AppConstant.UNPAID
        saleOrderService.updateStatusAndPercentage(invoice)
        saleInvoiceRepository.save(invoice)
        return ResponseObject(200, AppConstant.SUCCESS)
    }

    override fun updateStatusToPaid(id: Long): ResponseObject? {
        val invoice = findById(id)!!

        if (invoice.unpaidAmount == 0f)
            invoice.customStatus = AppConstant.PAID
        saleInvoiceRepository.save(invoice)
        return ResponseObject(200, AppConstant.SUCCESS)
    }

    override fun findAll(): List<SaleInvoice>? {
        return saleInvoiceRepository.findAllByStatusTrueOrderByIdDesc()
    }

    private fun checkInvoiceExceptions(invoice: SaleInvoice) {
        //Check require fields
        invoice.customer ?: throw CustomNotAcceptableException("Invalid Invoice require customer")
        invoice.totalQty ?: throw CustomNotAcceptableException("Invalid Invoice require totalQty")
        invoice.saleOrder ?: throw CustomNotAcceptableException("Invalid Invoice require saleOrder")
    }

    override fun findBySaleOrderId(id: Long): SaleInvoice? {
        return saleInvoiceRepository.findBySaleOrderIdAndStatusTrue(id)
    }

    override fun findAllByCustomerId(id: Long): List<SaleInvoice>? {
        return saleInvoiceRepository.findAllByCustomerId(id)
    }

    override fun sumUnpaidAmount(customerId: Long?): Double? {
        val unpiadAmount: Double? = saleInvoiceRepository.sumUnpaidAmount(customerId)
        if (unpiadAmount != null) {
            return unpiadAmount
        } else {
            return 0.0
        }

    }

    override fun calculateUnpaidAmount(id: Long, paidAmount: Float): ResponseObject? {
        val saleInvoice = saleInvoiceRepository.findByIdAndStatusTrue(id)!!
        saleInvoice.unpaidAmount = saleInvoice.unpaidAmount?.minus(paidAmount)
        saleInvoiceRepository.save(saleInvoice)
        return ResponseObject(200, AppConstant.SUCCESS)
    }

    override fun findByDeliveryOrderId(deliveryId: Long): SaleInvoice? {
        return saleInvoiceRepository.findByDeliveryNoteIdAndStatusTrue(deliveryId)
    }
}
