package com.example.demo.utilities

object AppConstant {

    /* APIs Uri */
    const val MAIN_PATH: String = "/v1/api"
    const val LIST_PATH: String = "/list"
    const val LIST_DTO_PATH: String = "/list-dto"
    const val ALL_PATH: String = "/all"
    const val UPDATE_SUBMIT_PATH: String = "/update-status-submit"
    const val UPDATE_CANCEL_PATH: String = "/update-status-cancel"
    const val UPDATE_STATUS_PATH: String = "/update-status"
    const val ALL_DROPDOWN: String = "/dropdown"


    /* Delivery Status */
    const val DRAFT: String = "Draft"
    const val COMPLETED: String = "Completed"
    const val SUBMIT: String = "Submit"
    const val CANCEL: String = "Cancelled"
    const val TO_BILL: String = "To Bill"
    const val TO_DELIVERY: String = "To Delivery"
    const val FULLY_BILLED: String = "Fully Billed"
    const val UNPAID: String = "Unpaid"
    const val PAID: String = "Paid"
    const val TO_DELIVER_AND_BILL: String = "To Deliver and Bill"
    const val TO_RECEIVE_AND_BILL: String = "To Receive and Bill"

    const val TO_RECEIVE: String = "To Receive"
    const val PARTIAL: String = "Partial"
    const val PARTIAL_Goods_Receipt: String = "Partial Goods Receipt"

    const val JOURNAL_MANUAL: Int = 1
    const val JOURNAL_AUTO: Int = 2

    const val JOURNAL_REVERSE: Int = 2

    const val BILL_PENDING: Int = 1
    const val BILL_CLEAR: Int = 2

    const val SUCCESS: String = "Success"

    const val BILL_CLEARED: String = "Bill cleared"
}
