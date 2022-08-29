package com.example.erpNew.model.model_sale


import com.example.erpNew.base.BaseEntity
import java.util.*
import javax.persistence.*

@Entity
data class Driver(

        var series: String? = null,

        var employeeId: String? = null,

        var driverName: String? = null,

        var cellphoneNumber: String? = null,

        var licenseNumber: String? = null,

        var issuingDate: Date? = null,

        var expiryDate: Date? = null

): BaseEntity()
