package com.example.demo.service.sale

import com.example.demo.model.sale.Invoice
import com.example.demo.model.sale.dto.InvoiceDTO
import com.example.demo.repository.sale.InvoiceDetailRepository
import com.example.demo.repository.sale.InvoiceRepository
import com.example.demo.utilities.UtilService
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import javax.persistence.criteria.Predicate


@Slf4j
@Service
@Qualifier("invV2")
class InvoiceServiceImpV2 : InvoiceService {

    @Autowired
    lateinit var invoiceRepository: InvoiceRepository
    @Autowired
    lateinit var invDetailRepo : InvoiceDetailRepository
    @Autowired
    lateinit var utilService: UtilService

    /**
     * @param allParams: are the dynamic filter params from JSON request params
     *
     * @allow doing custom DTO for collect only the needed fields for return.
     * @return Page of custom DTO format
     */
    override fun findAllList (allParams:Map<String,String>?): Page<InvoiceDTO> {

        val customName = allParams?.get("customerName")
        val invoiceNo = allParams?.get("invoiceNo")

        val page = allParams?.get("page")?.toInt() ?:0
        val size = allParams?.get("size")?.toInt() ?:10

        return invoiceRepository.findAll({ root, query, cb ->
            val predicates = ArrayList<Predicate>()
            customName?.let{
                val cusId = cb.like(root.get("customerName"), "%${it.trim().toUpperCase()}%")
                predicates.add(cusId)
            }

            invoiceNo?.let {
                val saleSeries = cb.like(root.get("invoiceNo"), "%${it.trim().toUpperCase()}%")
                predicates.add(saleSeries)
            }

            predicates.add(cb.isTrue(root.get("status")))
            query.orderBy(cb.asc(root.get<String>("id")))
            cb.and(*predicates.toTypedArray())
        },
            InvoiceDTO::class.java,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }


    /**
     * @sample : Update Object by write hard-code
     */
    fun updateOBJ (id:Long, invoice: Invoice) {
        val inv = invoiceRepository.getOne(id)

        inv.customerName = invoice.customerName
        inv.invoiceNo = invoice.invoiceNo
        inv.date = invoice.date
        inv.grandTotal = invoice.grandTotal
        inv.subTotal = invoice.subTotal
        inv.vatAmount = inv.vatAmount
        invoiceRepository.save(inv)
    }



    /**
     * @sample : Update Object by using BindProperties
     */
    fun updateOBJWithBindProperties (id:Long, invoice: Invoice){
        val inv = invoiceRepository.getOne(id)
        utilService.bindProperties(invoice, inv)
        invoiceRepository.save(inv)
    }


    /**
     * @sample : Update model that have Many-To-One relationship
     * @see : Using New style
     *
     *
     * @problem_solve by using :
     *      invoiceDetail.clear()
     *      inv.invoiceDetail?.addAll()
     */

    fun updateObj (id:Long, invoice: Invoice) {
        val inv = invoiceRepository.getOne(id)

        utilService.bindProperties(invoice, inv, exclude = listOf("invoiceDetail"))

        inv.invoiceDetail?.clear()
        inv.invoiceDetail?.addAll(invoice.invoiceDetail?: listOf())

        invoiceRepository.save(inv)
    }

    /**
     * @sample : Update model that have Many-To-One relationship
     *
     * @problems :
     *      database perform issues
     *      hard-code check condition
     */

    fun updateObjOldStyle (id:Long, invoice: Invoice) {
        val inv = invoiceRepository.getOne(id)

        inv.customerName = invoice.customerName
        inv.invoiceNo = invoice.invoiceNo
        inv.date = invoice.date
        inv.grandTotal = invoice.grandTotal
        inv.subTotal = invoice.subTotal
        inv.vatAmount = inv.vatAmount


        invoice.invoiceDetail?.map { detail ->

            // Case add Update Child
            if (detail.id != null){
                detail.invoice = inv
                detail.invoice = invDetailRepo.getOne(detail.id!!).invoice
            }

            // Case Add new Child
            if (detail.id == null){
                detail.invoice = inv
            }

            //Case Delete Child
            if (detail.status == false){
                invDetailRepo.delete(detail)
            }
        }

        inv.invoiceDetail = invoice.invoiceDetail

        invoiceRepository.save(inv)
    }

    override fun testThrow(i:Long){
        throw Exception("test throw form service")
    }
}















