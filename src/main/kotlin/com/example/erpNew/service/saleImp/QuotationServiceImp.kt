package com.ig.erp.service.implement.sale

import com.ig.erp.responseFormat.exception.CustomNotAcceptableException
import com.ig.erp.responseFormat.exception.CustomNotFoundException
import com.ig.erp.model.enum.GlobalSearchDocType
import com.ig.erp.model.model_sale.Quotation
import com.ig.erp.responseFormat.response.CustomResponseObj
import com.ig.erp.responseFormat.response.ResponseObject
import com.ig.erp.repository.sale.QuotationDetailRepository
import com.ig.erp.repository.sale.QuotationRepository
import com.ig.erp.service.implement.system.GlobalSearchServiceImpl
import com.ig.erp.service.implement.system.DocumentSettingServiceImpl
import com.ig.erp.service.services.QuotationService
import com.ig.erp.service.util.UtilService
import com.ig.erp.utils.AppConstant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.ArrayList
import javax.persistence.criteria.Predicate

@Service
class QuotationServiceImp : QuotationService {

    @Autowired
    lateinit var quotationRepository: QuotationRepository

    @Autowired
    lateinit var quotationDetailRepository: QuotationDetailRepository

    @Autowired
    lateinit var documentSettingService: DocumentSettingServiceImpl

    @Autowired
    lateinit var globalSearchServiceImpl: GlobalSearchServiceImpl

    @Autowired
    lateinit var utilService : UtilService

    override fun findAllList(q: String?, page: Int, size: Int): Page<Quotation>? {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): Quotation? {
        return quotationRepository.findByIdAndStatusTrue(id)
                ?: throw CustomNotFoundException("Quotation id $id doesn't exists")
    }

    override fun addNew(quotation: Quotation): Quotation? {

        quotation.series = documentSettingService.getNextSeries("quotation", quotation.isIncludeVat!!)
        checkQuotationExceptions(quotation)
        quotation.customStatus = AppConstant.DRAFT
        val t = quotationRepository.save(quotation)
        globalSearchServiceImpl.addGlobalSearch(GlobalSearchDocType.Quotation, t.series, t.id!!)
        return t
    }

    override fun updateObj(id: Long, t: Quotation): Quotation? {
        checkQuotationExceptions(t)
        val quote = findById(id)!!

        utilService.bindProperties(t, quote, exclude = listOf("quotationDetail"))
        quote.quotationDetail?.clear()
        quote.quotationDetail?.addAll(t.quotationDetail!!)
        return quotationRepository.save(quote)
    }

    override fun findAll(): List<Quotation>? {
        return quotationRepository.findAllByStatusTrueOrderByIdDesc()
    }

    override fun findFullCriteria(customerId: String?, series: String?, saleOrderSeries: String?, startDate: String?, endDate: String?, page: Int, size: Int): Page<Quotation>? {
        return quotationRepository.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()

            customerId?.let {
                predicates.add(cb.equal(root.get<String>("customer").get<Long>("id"), customerId))
            }
            series?.let {
                predicates.add(cb.like(root.get("series"), "%${series.trim().toUpperCase()}%"))
            }
            saleOrderSeries?.let {
                predicates.add(cb.like(root.get<String>("saleOrder").get("series"), "%${saleOrderSeries.trim().toUpperCase()}%"))
            }

            utilService.filterDateBetween("dateDelivery",startDate, endDate, cb, root)?.let { predicates.add(it) }

            predicates.add(cb.equal(root.get<String>("status"), true))
            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }

    override fun updatesStatusSubmit(id: Long): CustomResponseObj? {
        val quotation = findById(id)!!
        quotation.customStatus = AppConstant.SUBMIT
        quotationRepository.save(quotation)

        return CustomResponseObj(ResponseObject(200, AppConstant.SUCCESS), quotation.customStatus)
    }

    override fun updatesStatusComplete(id: Long): ResponseObject? {
        val quotation = findById(id)!!
        checkValidateQuotation(quotation)
        quotation.customStatus = AppConstant.COMPLETED
        quotationRepository.save(quotation)

        return ResponseObject(200, AppConstant.SUCCESS)
    }

    private fun checkValidateQuotation(quotation: Quotation) {
        // check submit quotation,
        // when create or convert to sale order
        quotation.saleOrder ?: throw CustomNotAcceptableException("Invalid Quotation require Sale Order")
    }

    private fun checkQuotationExceptions(quotation: Quotation) {
        /**
         * Check all require fields */
        quotation.series ?: throw CustomNotAcceptableException("Invalid Quotation require series")
    }
}
