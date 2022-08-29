package com.example.erpNew.service.saleImp


import com.example.erpNew.model.model_sale.DeliveryNote
import com.example.erpNew.model.model_sale.SaleInvoice
import org.springframework.stereotype.Service

@Service
class PostGLSaleService : PostGLService() {

    fun postPrepayment(t: CustomerPrePayment) {
        val glEntries = listOf(
                postGL(debit = t.amount, GLName = AccountMappingEnum.CustomerDeposit),
                postGL(credit = t.amount, GLName = AccountMappingEnum.CashOrBank, GLID = t.glAccount?.id)
        )
        setGLEntity(glEntries,voucherType = GLEntryVoucherType.CustomerPrepayment,customerId = t.customer!!.id,series = t.series!!,remark = t.remark)
    }


    fun postDeliveryOrder(t: DeliveryNote) {
        val glEntries = listOf(
                postGL(credit = t.grandTotal, GLName = AccountMappingEnum.Inventory),
                postGL(debit = t.grandTotal, GLName = AccountMappingEnum.ShippingClearing)
        )
        setGLEntity(glEntries,voucherType = GLEntryVoucherType.SaleDeliveryOrder,customerId = t.customer!!.id,series = t.series!!,remark = t.note)

    }


    fun postSaleInvoice(t: SaleInvoice) {
        val glEntries = listOf(
                postGL(debit = t.totalCost, GLName = AccountMappingEnum.COGS),
                postGL(credit = t.totalCost, GLName = AccountMappingEnum.ShippingClearing),

                postGL(credit = t.grandTotal?.toDouble(), GLName = AccountMappingEnum.SaleRevenue),
                postGL(debit = t.grandTotal?.toDouble(), GLName = AccountMappingEnum.AR)
        )

        setGLEntity(glEntries,voucherType = GLEntryVoucherType.SaleInvoice,customerId = t.customer!!.id,series = t.series!!,remark = "")
    }


    fun postPaymentEntry(t: CustomerPaymentEntry) {
        val glEntries = mutableListOf<GLEntry>()


        //case pay with prepayment
        if (t.totalPrepayment!! > 0.0) {
            glEntries.addAll(listOf(
                    postGL(debit = t.totalPrepayment, GLName = AccountMappingEnum.CustomerDeposit),
                    postGL(credit = t.totalPrepayment, GLName = AccountMappingEnum.AR))
            )
        }
        else {
            glEntries.addAll(listOf(
                postGL(debit = t.totalAmount, GLName = AccountMappingEnum.CashOrBank, GLID = t.glAccount?.id),
                postGL(credit = t.totalAmount, GLName = AccountMappingEnum.AR))
            )
        }

        setGLEntity(glEntries,voucherType = GLEntryVoucherType.CustomerSettlement,customerId = t.customer!!.id,series = t.series!!,remark = t.remark)

    }


    fun postSaleReturn(t: SaleReturn) {
        val glEntries = listOf(
                postGL(debit = t.grandTotal, GLName = AccountMappingEnum.Inventory),
                postGL(credit = t.grandTotal, GLName = AccountMappingEnum.ShippingClearing)
        )

        setGLEntity(glEntries,voucherType = GLEntryVoucherType.SaleReturn,customerId = t.customer!!.id,series = t.series!!,remark = t.remark)
    }


    fun postCreditNote(t: CreditNote) {

        val glEntries = listOf(
                postGL(debit = t.invoiceAmount, GLName = AccountMappingEnum.SaleRevenue),
                postGL(credit = t.invoiceAmount, GLName = AccountMappingEnum.AR),

                postGL(credit = t.invoice?.totalCost, GLName = AccountMappingEnum.COGS),
                postGL(debit = t.invoice?.totalCost, GLName = AccountMappingEnum.ShippingClearing)
        )

        setGLEntity(glEntries,voucherType = GLEntryVoucherType.SaleCreditNote,customerId = t.customer!!.id,series = t.series!!,remark = t.remark)

    }


    fun postRefund(t: Refund) {
        val glEntries = listOf<GLEntry>()

        //case credit with prepayment
        if (t.paymentOption == PaymentOption.Prepayment.toString()) {
            listOf(
                    postGL(debit = t.refundAmount, GLName = AccountMappingEnum.CustomerDeposit),
                    postGL(credit = t.refundAmount, GLName = AccountMappingEnum.Cash)
            )


        // case select cash or bank or cheque
        } else {
            listOf(
                    postGL(debit = t.refundAmount, GLName = AccountMappingEnum.CashOrBank, GLID = t.glAccount?.id),
                    postGL(credit = t.refundAmount, GLName = AccountMappingEnum.AR)
            )
        }
        setGLEntity(glEntries,voucherType = GLEntryVoucherType.SaleRefund,customerId = t.customer!!.id,series = t.series!!,remark = t.remark)
    }
}
