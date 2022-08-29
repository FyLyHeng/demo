package com.example.erpNew.service.status

interface IStatusService {

    fun updateToCancel(id: Long)
    fun updateToComplete(id: Long)
    fun updateToDraft(id: Long)
    fun updateToSubmit(id: Long)

}


interface ISaleStatusService {

    fun updateToBill(id: Int)
    fun updateToVoid(id: Int)

}


interface IPaymentStatusService {
    fun updateToPaid(id: Int)
    fun updateToUnpaid(id: Int)
}