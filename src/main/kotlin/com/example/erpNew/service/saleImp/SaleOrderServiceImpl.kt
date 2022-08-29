package com.example.erpNew.service.saleImp


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import javax.transaction.Transactional
import kotlin.collections.ArrayList

@Service
class SaleOrderServiceImpl  {

    @Autowired
    lateinit var saleOrderRepository: SaleOrderRepository
    @Autowired
    lateinit var saleOrderDetailRepository: SaleOrderDetailRepository
    @Autowired
    lateinit var saleTaxChargeService: SaleTaxChargeService
    @Autowired
    lateinit var saleInvoiceService: SaleInvoiceService
    @Autowired
    lateinit var saleInvoiceRepository : SaleInvoiceRepository
    @Autowired
    lateinit var stockBalanceService: StockBalanceService
    @Autowired
    lateinit var currencyService: CurrencyService
    @Autowired
    lateinit var customerService: CustomerService
    @Autowired
    lateinit var salePersonService: SalePersonService
    @Autowired
    lateinit var balanceService: BalanceService
    @Autowired
    lateinit var warehouseService: WarehouseService
    @Autowired
    lateinit var priceListService: PriceListService
    @Autowired
    lateinit var itemService: ItemService
    @Autowired
    lateinit var quotationService: QuotationService
    @Autowired
    lateinit var documentSettingService: DocumentSettingServiceImpl
    @Autowired
    lateinit var globalSearchServiceImpl: GlobalSearchServiceImpl
    @Autowired
    lateinit var utilService: UtilService
    @Autowired
    lateinit var customerPrePaymentService: CustomerPaymentService
    @Autowired
    lateinit var companyService: CompanyService
    @Autowired
    lateinit var deliveryNoteRepository: DeliveryNoteRepository

//####### Basic CRUD Service ################################################################################

    override fun findAllList(q: String?, page: Int, size: Int): Page<SaleOrder>? {
        return saleOrderRepository.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()
            if (q != null) {
                val series = cb.like(cb.upper(root.get("series")), "%${q.toUpperCase()}%")
                val shippingAddress = cb.like(cb.upper(root.get("shippingAddress")), "%${q.toUpperCase()}%")
                predicates.add(cb.or(series, shippingAddress))
            }
            predicates.add(cb.isTrue(root.get<Boolean>("status")))
            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }

    override fun findById(id: Long): SaleOrder? {
        return saleOrderRepository.findByIdAndStatusTrue(id)
                ?: throw CustomNotFoundException("Sale Order id $id doesn't exists")
    }

    override fun findShortById(id: Long): CustomSaleOrder? {
        val obj = findById(id)
        val warehouseCustom = WarehouseCustom(obj?.warehouse?.id, obj?.warehouse?.warehouseName, obj?.warehouse?.phone, obj?.warehouse?.addressLine1, obj?.warehouse?.addressLine2)
        return CustomSaleOrder(warehouseCustom, obj?.saleOrderDetail)
    }

    override fun findAll(): List<SaleOrder>? {
        return saleOrderRepository.findAllByStatusTrueOrderByIdDesc()
    }

    override fun findAllByCustomerId(id: Long): List<SaleOrder>? {
        return saleOrderRepository.findAllByCustomerId(id)
    }

    override fun findAll(q: String?, customStatus: MutableList<String>?): MutableList<SaleOrder> {
        return saleOrderRepository.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()
            if (q != null) {
                val series = cb.like(cb.upper(root.get("series")), "%${q.toUpperCase()}%")
                val cusName = cb.like(cb.upper(root.get<String>("customer").get("name")), "%${q.toUpperCase()}%")
                val cusSeries = cb.like(cb.upper(root.get<String>("customer").get("series")), "%${q.toUpperCase()}%")

                predicates.add(cb.or(series, cusName,cusSeries))
            }
            customStatus?.let {
                predicates.add(root.get<Path<String>>("customStatus").`in`(customStatus))
            }

            predicates.add(cb.isTrue(root.get("status")))
            cb.and(*predicates.toTypedArray())
        }, Sort.by(Sort.Direction.DESC, "id"))
    }

    override fun findAll(allParams: MutableMap<String, String>?): MutableList<SaleOrder>? {
        val customerId: Long? = allParams?.get("customerId")?.toLong()
        return saleOrderRepository.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()
            if (customerId != null) {
                predicates.add(cb.equal(root.get<Customer>("customer").get<Long>("id"), customerId))
            }

            predicates.add(cb.like(root.get("customStatus"), AppConstant.TO_DELIVER_AND_BILL))
            predicates.add(cb.isTrue(root.get("status")))
            cb.and(*predicates.toTypedArray())
        }, Sort.by(Sort.Direction.DESC, "id"))
    }

    override fun findFullCriteria(query:String?, customStatus: MutableList<String>?, page: Int, size: Int): Page<SaleOrder> {
        return saleOrderRepository.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()

            if (query != null) {
                val series = cb.like(cb.upper(root.get("series")), "%${query.toUpperCase()}%")
                val cusName = cb.like(cb.upper(root.get<String>("customer").get("name")), "%${query.toUpperCase()}%")
                val cusSeries = cb.like(cb.upper(root.get<String>("customer").get("series")), "%${query.toUpperCase()}%")

                predicates.add(cb.or(series, cusName,cusSeries))
            }
            customStatus?.let {
                predicates.add(root.get<Path<String>>("customStatus").`in`(customStatus))
            }

            predicates.add(cb.isTrue(root.get("status")))
            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }

    override fun findFullCriteriaDTO(
        query: String?,
        customStatus: MutableList<String>?,
        page: Int,
        size: Int
    ): Page<SaleOrderDTO> {
        return saleOrderRepository.findAll({ root, cq, cb ->
            val predicates = ArrayList<Predicate>()

            if (query != null) {
                val series = cb.like(cb.upper(root.get("series")), "%${query.toUpperCase()}%")
                val cusName = cb.like(cb.upper(root.get<String>("customer").get("name")), "%${query.toUpperCase()}%")
                val cusSeries = cb.like(cb.upper(root.get<String>("customer").get("series")), "%${query.toUpperCase()}%")
                predicates.add(cb.or(series, cusName,cusSeries))
            }

            val cus = root.join<SaleOrder, Customer>("customer", JoinType.LEFT)
            cq.multiselect(root.get<Long>("id"), root.get<String>("series"), cus.get<Long>("id"), cus.get<String>("name"))
            cq.distinct(true)


            customStatus?.let {
                predicates.add(root.get<Path<String>>("customStatus").`in`(customStatus))
            }

            predicates.add(cb.isTrue(root.get("status")))
            cb.and(*predicates.toTypedArray())
        }
            ,SaleOrderDTO::class.java
            ,PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }

    @Transactional
    override fun addNew(t: SaleOrder): SaleOrder? {
        t.series = documentSettingService.getNextSeries("saleOrder", t.isIncludeVat!!)
        checkSaleOrderExceptions(t)
        t.customStatus = AppConstant.DRAFT
        t.billedStatus = SaleOrderStatus.NotBilled.toString()
        t.deliveryStatus = SaleOrderStatus.NotDelivered.toString()
        t.perBilled = 0f
        t.perDelivered = 0f

        t.saleOrderDetail?.map {
            it.saleOrder = t
            it.remainQty = it.qty
        }

        if (t.quotation?.id != null) quotationService.updatesStatusComplete(t.quotation?.id!!)

        if (t.isDeposit!!) {
            this.createDeposit(t)
        }
        val saleOrder = saleOrderRepository.save(t)
        globalSearchServiceImpl.addGlobalSearch(GlobalSearchDocType.SaleOrder, saleOrder.series, saleOrder.id)
        return saleOrder
    }

    override fun updateObj(id: Long, t: SaleOrder): SaleOrder? {
        val saleOrder = saleOrderRepository.findByIdAndStatusTrue(id)!!
        if (!t.status) saleOrder.status = false
        else {
            checkSaleOrderExceptions(t)
            utilService.bindProperties(t, saleOrder, exclude = listOf("saleOrderDetail"))

            t.saleOrderDetail?.map {
                it.amount = it.qty.times(it.rate)
                it.remainQty = it.qty
                it.stockQty = it.conversionFactor!! * it.qty
            }
            saleOrder.saleOrderDetail?.clear()
            saleOrder.saleOrderDetail?.addAll(t.saleOrderDetail!!)
        }

        return saleOrderRepository.save(saleOrder)
    }

    override fun getSaleOrderDropDown(): SaleOrderDropDown? {

        return SaleOrderDropDown(
            currencyList = currencyService.findAll(),
            customerList = customerService.findAll(),
            salePersonList = salePersonService.getSalePersonCustom(),
            balanceList = balanceService.findAll(),
            warehouseList = warehouseService.findAllCustomWarehouse(),
            priceListList = priceListService.findAllCustomPriceList(),
            itemList = itemService.findAllCustomItem(),
            saleTaxChargeList = saleTaxChargeService.findAll()
        )
    }

    override fun getSaleOrderSeriesDropDown(): List<SaleOrderSeriesDropDown>? {
        return saleOrderRepository.findAllByStatusTrueOrderByIdAsc()?.map {
            SaleOrderSeriesDropDown(it.id, it.series)
        }
    }




    /**
     * CANCEL SALE ORDER
     *  - update status sale order to `cancel`
     *  - then cancel reserved qty stock balance
     */
    override fun updateStatusCancel(id: Long): ResponseObject? {
        val saleOrder = findById(id)!!
        saleOrder.customStatus = AppConstant.CANCEL


        //TODO move to fun deduct Stock
        saleOrder.saleOrderDetail?.forEach {
            stockBalanceService.cancelReservedQuantity(saleOrder.warehouse!!.id!!, it.item!!.id!!,it.qty, it.conversionFactor!! )
        }

        saleOrderRepository.save(saleOrder)
        return ResponseObject(200, AppConstant.SUCCESS)
    }

    /**
     * SUBMIT SALE ORDER
     *  update status sale order to `deliveryAndBill`
     *  then add reserved qty stock balance
     */
    override fun updateStatusToDeliverAndBill(id: Long): ResponseObject? {
        val saleOrder = findById(id)!!
        val company = companyService.getCurrentCompany(3)!!

        saleOrder.customStatus = AppConstant.TO_DELIVER_AND_BILL

        if (company.deductStockSetting == DeductStockSetting.SaleOrder) {
            saleOrder.customStatus = AppConstant.TO_BILL
            this.autoDummyDeliveryNote(saleOrder)
        }

        //TODO move to fun deduct Stock
        saleOrder.saleOrderDetail?.forEach {
            stockBalanceService.addReservedQuantity(saleOrder.warehouse!!.id!!, it.item!!.id!!,it.qty, it.conversionFactor!!)
        }

        saleOrderRepository.save(saleOrder)
        return ResponseObject(200, AppConstant.SUCCESS)
    }


    override fun updateSaleOrderCustomStatus(saleOrder: SaleOrder): ResponseObject? {
        val fullyDelivered = SaleOrderStatus.FullyDelivered.toString()
        val fullyBilled = SaleOrderStatus.FullyBilled.toString()


        val saleOrderStatus = when {
            (saleOrder.deliveryStatus != fullyDelivered && saleOrder.billedStatus != fullyBilled) -> {AppConstant.TO_DELIVER_AND_BILL}
            (saleOrder.deliveryStatus != fullyDelivered && saleOrder.billedStatus == fullyBilled) -> {AppConstant.TO_DELIVERY}
            (saleOrder.deliveryStatus == fullyDelivered && saleOrder.billedStatus != fullyBilled) -> {AppConstant.TO_BILL}
            (saleOrder.deliveryStatus == fullyDelivered && saleOrder.billedStatus == fullyBilled) -> {AppConstant.COMPLETED}
            else -> throw CustomNotAcceptableException("Pls context Admin")
        }

        saleOrder.customStatus = saleOrderStatus
        saleOrderRepository.save(saleOrder)
        return ResponseObject(200, AppConstant.SUCCESS)
    }


//####### Validation Expost Support Service ################################################################################

    override fun isCustomerOnSaleOrder(customerId: Long): Boolean {
        return saleOrderRepository.existsByCustomerIdAndStatusTrue(customerId)
    }

    override fun isItemPriceOnSaleOrder(itemPriceId: Long): Boolean {
        return saleOrderRepository.existsByPriceListIdAndStatusTrue(itemPriceId)
    }

    override fun isSaleOrderOnSaleOrder(saleOrderId: Long): Boolean {
        return saleOrderRepository.existsBySalePersonIdAndStatusTrue(saleOrderId)
    }

    override fun isValidCreditLimit(saleOrderId: Long): Boolean {
        val so: SaleOrder? = findById(saleOrderId)
        val customer: Customer? = so?.customer
        if (customer?.passCreditSaleOrder == true) { // skip check credit limit if true
            return true
        } else if (customer?.creditLimit!! > 0) { // check credit limit

            var unpaidInvoiceAmount = saleInvoiceService.sumUnpaidAmount(customer.id)
            return false

        } else {
            return true
        }
    }

    override fun updateFirstProcessFlow(saleOrder: SaleOrder, firstProcessStatus: SaleOrderStatus) {

        if (saleOrder.firstProcessFlow == null){
            saleOrder.firstProcessFlow = firstProcessStatus
        }
    }


//####### DeliveryNote Support Service ####################################################################
    /**
     * UPDATE STATUS DELIVERY FROM `TO_BILL` BACK TO `DRAFT`
     *  when cancel delivery function will trigger
     *   - update status sale order to `draft`
     *   - return item qty stock balance by warehouse
     */
    override fun backStatusToDraft(id: Long): ResponseObject? {
        val saleOrder = findById(id)!!
        saleOrder.customStatus = AppConstant.DRAFT


        //TODO move to fun deduct Stock
        saleOrder.saleOrderDetail?.forEach {
            stockBalanceService.returnQuantity(saleOrder.warehouse!!.id!!, it.item!!.id!!,it.qty, it.conversionFactor!!, it.rate)
        }

        saleOrderRepository.save(saleOrder)
        return ResponseObject(200, AppConstant.SUCCESS)
    }

    /**
     * COMPLETE SALE ORDER
     *  - update sale order status to `complete`
     *  - update sale invoice status to `unpaid`
     *      - remove stock balance qty by warehouse then the function remove_balance_qty will automate
     *          cancel reserved qty from stock
     */
    override fun updateStatusCompleted(id: Long): ResponseObject? {
        val saleOrder = findById(id)!!

        if (saleOrder.billedStatus === SaleOrderStatus.PartlyBill.toString())
            saleOrder.customStatus = AppConstant.TO_DELIVER_AND_BILL
        else saleOrder.customStatus = AppConstant.COMPLETED


        //TODO move to fun deduct Stock
        saleOrder.saleOrderDetail?.forEach {
            stockBalanceService.confirmReservedQuantity(saleOrder.warehouse!!.id!!, it.item!!.id!!, it.qty, it.conversionFactor!!, it.valuationRate!!, saleOrder.series, "")
        }

        saleOrderRepository.save(saleOrder)
        return ResponseObject(200, AppConstant.SUCCESS)
    }

    /**
     * - function update delivery status
     *   check if -> update status bill after submit DO
     */
    override fun updateDeliveryStatus(deliveryNote: DeliveryNote): CustomResponseObj? {
        if (deliveryNote.saleOrder?.firstProcessFlow == SaleOrderStatus.DeliveryOrder)
            deliveryNote.customStatus  = AppConstant.TO_BILL
        else {
            deliveryNote.customStatus  = AppConstant.COMPLETED
        }

        return CustomResponseObj(ResponseObject(200, AppConstant.SUCCESS), deliveryNote.customStatus)
    }

    override fun calculatePrepayment(saleOrderId: Long, prepaymentAmount: Double) {
        val saleOrder = findById(saleOrderId)!!
        saleOrder.prepaymentAmount = saleOrder.prepaymentAmount?.plus(prepaymentAmount)
        saleOrder.prepaymentBalance = saleOrder.prepaymentBalance?.minus(prepaymentAmount)
        saleOrderRepository.save(saleOrder)
    }

    /**
     * when submit delivery order
     * - update billed Status to => fully or partly
     * - update billed percent => 100 or ?%
     */
    //TODO
    override fun updateBilledStatusAndPercentage(deliveryNote: DeliveryNote): ResponseObject? {
        val saleOrder = saleOrderRepository.findByIdAndStatusTrue(deliveryNote.saleOrder?.id!!)
            ?:throw CustomNotAcceptableException("The given id doesn't exist")

        val totalQty = saleOrder.totalQty?.toFloat()
        val totalDeliveredQty = saleOrderDetailRepository.sumDeliveryQtyBySaleOrderId(saleOrder.id!!)

        if (totalQty!! > totalDeliveredQty!!) {
            saleOrder.billedStatus = SaleOrderStatus.PartlyBill.toString()
            saleOrder.perBilled = findPercentage(totalDeliveredQty, totalQty)

        } else if (totalQty == totalDeliveredQty) {
            saleOrder.billedStatus = SaleOrderStatus.FullyBilled.toString()
            saleOrder.perBilled = 100f // 100%
        }

        saleOrderRepository.save(saleOrder)

        return ResponseObject(200, AppConstant.SUCCESS)
    }

    /**
     * when submit delivery order
     * - update delivery Status to => fully or partly
     * - update Delivered percent => 100 or ?%
     */

    //TODO
    override fun updateDeliveryStatusAndPercentage(deliveryNote: DeliveryNote): ResponseObject? {
        val saleOrder = saleOrderRepository.findByIdAndStatusTrue(deliveryNote.saleOrder?.id!!)
            ?:throw CustomNotAcceptableException("The given id doesn't exist")

        val totalQty = saleOrder.totalQty?.toFloat()
        val totalDeliveredQty = saleOrderDetailRepository.sumDeliveryQtyBySaleOrderId(saleOrder.id!!)

        if (totalQty!! > totalDeliveredQty!!) {
            saleOrder.deliveryStatus = SaleOrderStatus.PartlyDelivered.toString()
            saleOrder.perDelivered = findPercentage(totalDeliveredQty, totalQty)
        } else if (totalQty == totalDeliveredQty) {
            saleOrder.deliveryStatus = SaleOrderStatus.FullyDelivered.toString()
            saleOrder.perDelivered = 100f // 100%
        }
        saleOrderRepository.save(saleOrder)
        return ResponseObject(200, AppConstant.SUCCESS)
    }



//####### Invoice Support Service #########################################################################
    /**
     * when submit Invoice order
     * - update customStatus
     * - update billed Status to => fully or partly
     * - update billed percent => 100 or ?%
     */
    override fun updateStatusAndPercentage(saleInvoice: SaleInvoice): ResponseObject? {

        val saleOrder = saleOrderRepository.findByIdAndStatusTrue(saleInvoice.saleOrder?.id!!)
            ?:throw CustomNotAcceptableException("The given id doesn't exist")

        val totalQty = 100f
        val totalInvoiceIssueQty = saleInvoiceRepository.sumInvoicePerBySaleOrderId(saleOrder.id!!)!!

        if (totalQty > totalInvoiceIssueQty) {
            saleOrder.billedStatus = SaleOrderStatus.PartlyBill.toString()
            saleOrder.perBilled = totalInvoiceIssueQty

        } else if (totalQty == totalInvoiceIssueQty) {
            saleOrder.billedStatus = SaleOrderStatus.FullyBilled.toString()
            saleOrder.perBilled = 100f
            //saleOrder.customStatus = AppConstant.TO_DELIVERY //TODO chnage to use fun updateSaleOrderCustomStatus
        }

        return ResponseObject(200, AppConstant.SUCCESS)
    }



//####### Private Support Service #########################################################################

    private fun checkSaleOrderExceptions(saleOrder: SaleOrder) {
        /**
         * Check all require fields */
        saleOrder.series ?: throw CustomNotAcceptableException("Invalid Sale Order require series")
    }
    private fun findPercentage(totalDeliveryQty: Float, totalSaleQty: Float): Float {
        return totalDeliveryQty.div(totalSaleQty).times(100)
    }

    private fun createDeposit(saleOrder: SaleOrder) {
        val customerPayment = saleOrder.customerDeposit!!
        customerPayment.customer = saleOrder.customer
        customerPayment.isBySO = true
        customerPayment.customerPrePaymentReference?.map {
            it.saleOrder = saleOrder
            it.amount = saleOrder.customerDeposit!!.amount!!
        }

        customerPrePaymentService.addNew(customerPayment)
    }

    private fun autoDummyDeliveryNote(saleOrder: SaleOrder) {

        val deliveryNote = DeliveryNote(
                customStatus = AppConstant.COMPLETED,
                saleOrder = saleOrder,
                totalQty = saleOrder.totalQty,
                total = saleOrder.total,
                vatAmount = saleOrder.vatAmount,
                additionalDisAmount = saleOrder.additionalDisAmount?.toDouble(),
                grandTotal = saleOrder.grandTotal?.toDouble(),
                deliveryNoteDetail = ArrayList()
        )

        saleOrder.saleOrderDetail?.forEach {
            val deliveryNoteDetail = DeliveryNoteDetail(
                    qty = it.qty,
                    amount = it.amount,
                    rate = it.qty * it.amount!!,
                    stockQty = it.stockQty,
                    conversionFactor = it.conversionFactor,
                    discountPercent = it.discountPercent,
                    discount = it.discount,
                    serialNo = it.serialNo,
                    HasSerialNo = it.hasSerialNo!!,
                    item = it.item,
                    itemVariantUom = it.itemVariantUom

            )

            deliveryNote.deliveryNoteDetail?.add(deliveryNoteDetail)
        }

        deliveryNoteRepository.save(deliveryNote)
    }
}
