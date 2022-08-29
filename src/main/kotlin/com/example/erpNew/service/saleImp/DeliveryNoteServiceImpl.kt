package com.example.erpNew.service.saleImp

import com.example.erpNew.model.model_sale.DeliveryNote
import com.example.erpNew.service.sale.DeliveryNoteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.persistence.criteria.Predicate
import javax.transaction.Transactional

@Service
class DeliveryNoteServiceImpl : DeliveryNoteService {

    @Autowired
    lateinit var deliveryNoteRepository: DeliveryNoteRepository
    @Autowired
    lateinit var deliveryTypeRepository: DeliveryTypeRepository
    @Autowired
    lateinit var saleOrderService: SaleOrderService
    @Autowired
    lateinit var saleOrderDetailService: SaleOrderDetailService
    @Autowired
    lateinit var driverRepository: DriverRepository
    @Autowired
    lateinit var stockBalanceService: StockBalanceService
    @Autowired
    lateinit var saleOrderRepository: SaleOrderRepository
    @Autowired
    lateinit var documentSettingService: DocumentSettingServiceImpl
    @Autowired
    lateinit var postGLSaleService: PostGLSaleService
    @Autowired
    lateinit var globalSearchServiceImpl: GlobalSearchServiceImpl
    @Autowired
    lateinit var serialNoService: SerialNoService
    @Autowired
    lateinit var utilService : UtilService


    override fun findAll(): List<DeliveryNote> {
        return deliveryNoteRepository.findAllByStatusTrueOrderByIdDesc()
    }

    override fun findAllList(allParams: MutableMap<String, String>, page: Int, size: Int): Page<DeliveryNote> {
        return deliveryNoteRepository.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()
            if (q != null) {
                val customerId = cb.equal(root.get<String>("customer").get<Long>("id"), q)
                predicates.add(customerId)
            }
            predicates.add(cb.isTrue(root.get("status")))
            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }

    override fun findFullCriteria(customerId: String?, saleOrderSeries: String?, deliverySeries: String?, startDate: String?, endDate: String?, immediateTransfer: Boolean, page: Int, size: Int): Page<DeliveryNote>? {
        return deliveryNoteRepository.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()
            if (customerId != null) {
                val cusId = cb.equal(root.get<String>("customer").get<Long>("id"), customerId)
                predicates.add(cusId)
            }
            if (saleOrderSeries != null) {
                val saleSeries = cb.like(root.get<String>("saleOrder").get("series"), "%${saleOrderSeries.trim().toUpperCase()}%")
                predicates.add(saleSeries)
            }
            if (deliverySeries != null) {
                val delSeries = cb.like(root.get("series"), "%${deliverySeries.trim().toUpperCase()}%")
                predicates.add(delSeries)
            }

            if (startDate != null && endDate != null) {
                val formatter = SimpleDateFormat("yyyy/MM/dd")
                try {
                    val start = formatter.parse(startDate)
                    val end = formatter.parse(endDate)

                    val deliveryDate = cb.between(root.get<String>("saleOrder").get("dateDelivery"), start, end)
                    predicates.add(deliveryDate)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
            if (immediateTransfer) {
                predicates.add(cb.equal(root.get<String>("immediateTransfer"), immediateTransfer))
            }
            predicates.add(cb.equal(root.get<String>("status"), true))
            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }

    override fun findBySaleOrderId(id: Long): DeliveryNote? {
        return deliveryNoteRepository.findBySaleOrderIdAndStatusTrue(id)
    }

    override fun findByCustomerId(id: Long) : List<DeliveryNote>? {
        return deliveryNoteRepository.findAllByCustomerId(id)?.sortedByDescending { it.id }
    }

    override fun isExistSaleOrderId(id: Long): Boolean {
        return deliveryNoteRepository.existsBySaleOrderIdAndStatusTrue(id)
    }

    override fun findById(id: Long): DeliveryNote? {
        return deliveryNoteRepository.findByIdAndStatusTrue(id)
                ?: throw CustomNotAcceptableException("Delivery id $id doesn't exist")
    }

    @Transactional
    override fun addNew(deliveryNote: DeliveryNote): DeliveryNote? {
        checkDeliveryExceptions(deliveryNote)
        val saleOrder = saleOrderRepository.findByIdAndStatusTrue(deliveryNote.saleOrder?.id!!)!!

        when (saleOrder.perDelivered){
            100f -> throw CustomNotAcceptableException("You already completed Delivery Note")
        }


        deliveryNote.series = documentSettingService.getNextSeries("deliveryOrder")
        // related to sipping and billing address
        deliveryNote.shippingAddress = saleOrder.shippingAddress
        deliveryNote.shippingAddressId = saleOrder.billingAddressId
        deliveryNote.shippingTitle = saleOrder.shippingTitle
        deliveryNote.billingAddress = saleOrder.billingAddress
        deliveryNote.billingAddressId = saleOrder.billingAddressId
        deliveryNote.billingTitle = saleOrder.billingTitle

        deliveryNote.deliveryNoteDetail?.forEach {

            if (it.item!!.HasSerialNo!!){
                val qty = (it.qty!! * it.conversionFactor!!).toInt()
                serialNoService.updateValidation(it.item!!, it.serialNo, qty)
            }
        }

        val saleDeliveryOrder = deliveryNoteRepository.save(deliveryNote)
        globalSearchServiceImpl.addGlobalSearch(GlobalSearchDocType.SaleDelivery, saleDeliveryOrder.series, saleDeliveryOrder.id)
        return saleDeliveryOrder
    }

    override fun updateObj(id: Long, t: DeliveryNote): DeliveryNote? {
        val delivery = this.findById(id)!!
        checkDeliveryExceptions(t)
        utilService.bindProperties(t,delivery, exclude = listOf("deliveryNoteDetail"))

        t.deliveryNoteDetail?.forEach {
            if (it.item!!.HasSerialNo!!){
                val qty = (it.qty!! * it.conversionFactor!!).toInt()
                serialNoService.updateValidation(it.item!!, it.serialNo, qty)
            }
        }

        delivery.deliveryNoteDetail?.clear()
        delivery.deliveryNoteDetail?.addAll(t.deliveryNoteDetail!!)
        return deliveryNoteRepository.save(delivery)
    }

    /**
     * COMPLETE DELIVERY ORDER
     *  update delivery order to `completed`
     *  update billed status and percentage
     *  update sale order to `completed`
     *  update sale invoice status to `unpaid`
     */
    override fun updateStatusCompleted(id: Long): ResponseObject? {
        val delivery = deliveryNoteRepository.findByIdAndStatusTrue(id)
                ?:throw CustomNotAcceptableException("The given id doesn't exist")

        delivery.customStatus = AppConstant.COMPLETED
        delivery.dateDone = Date()
        deliveryNoteRepository.save(delivery)

        saleOrderService.updateBilledStatusAndPercentage(delivery)
        saleOrderService.updateStatusCompleted(delivery.saleOrder?.id!!)

        postGLSaleService.postDeliveryOrder(delivery)
        return ResponseObject(200, AppConstant.SUCCESS)
    }

    /**
     * CANCEL DELIVERY ORDER
     *  - update delivery status to `cancel`
     *  - update sale order status to `draft`
     *  - function update sale order status `draft` will automate
     *      return item qty stock balance to warehouse
     */
    override fun updateStatusCancel(id: Long): ResponseObject? {
        val delivery = deliveryNoteRepository.findByIdAndStatusTrue(id)
        delivery?.customStatus = AppConstant.CANCEL
        deliveryNoteRepository.save(delivery!!)
        saleOrderService.backStatusToDraft(delivery.saleOrder?.id!!)
        return ResponseObject(200, AppConstant.SUCCESS)
    }

    /**
     * SUBMIT DELIVERY ORDER
     *    call updateFirstProcessFlow function
     *    call updateDeliveryStatusAndPercentage function
     *    call updateDeliveryStatus function
     *  - function validateAllowedDeliveryQty
     *    if allowed qty < delivery qty alert message
     */
    @Transactional
    override fun updateStatusToBill(id: Long): CustomResponseObj? {
        val delivery = deliveryNoteRepository.findByIdAndStatusTrue(id)

        saleOrderService.updateFirstProcessFlow(delivery?.saleOrder!!, SaleOrderStatus.DeliveryOrder)
        saleOrderService.updateDeliveryStatusAndPercentage(delivery)
        saleOrderService.updateSaleOrderCustomStatus(delivery.saleOrder!!)
        saleOrderDetailService.updateDeliveredQty(delivery)

        val deliveryOrder = saleOrderService.updateDeliveryStatus(delivery)


        /**
         * TODO need to check deduct Stock
         * Remove Qty from Stock (cut stock)
         * Update Serial-No Warranty for each Item
         */
        delivery.deliveryNoteDetail?.map {

            var serialNo : String? = null
            if (it.item!!.HasSerialNo!! && it.status)
                serialNo = serialNoService.updateSerialWarranty(delivery, it)

            stockBalanceService.confirmReservedQuantity(delivery.warehouse!!.id!!,it.item!!.id!!, it.qty!!, it.conversionFactor!!, it.rate!!,delivery.series, serialNo)
        }

        return CustomResponseObj(ResponseObject(200, AppConstant.SUCCESS), deliveryOrder?.paymentStatus)
    }


    override fun updateStatusImmediateTransfer(id: Long, immediateTransfer: Boolean): ResponseObject? {
        val delivery = deliveryNoteRepository.findByIdAndStatusTrue(id)
        delivery?.immediateTransfer = immediateTransfer
        deliveryNoteRepository.save(delivery!!)
        return ResponseObject(200, AppConstant.SUCCESS)
    }

    override fun getDeliveryOrderDropDown(): DeliveryDropDown? {
        return DeliveryDropDown(deliveryTypeRepository.findAllByStatusTrueOrderByIdDesc())
    }

    override fun getDriverDropDown(): List<CustomDriverResponse>? {
        val driver = driverRepository.findAllByStatusTrueOrderByIdDesc()
        val driverCustom = ArrayList<CustomDriverResponse>()
        driverCustom.addAll(driver?.map { CustomDriverResponse(it.id, it.driverName, it.cellphoneNumber) }!!)
        return driverCustom
    }

    override fun isDriverOnDelivery(driverId: Long): Boolean {
        return deliveryNoteRepository.existsByDriverIdAndStatusTrue(driverId)
    }

    private fun checkDeliveryExceptions(deliveryNoteTest: DeliveryNote) {
        /**
         * Check require fields */
        if (deliveryNoteTest.saleOrder == null) throw CustomNotAcceptableException("Invalid Delivery Order require saleOrder")
    }
}
