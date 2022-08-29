package com.example.erpNew.model.model_sale

import com.example.erpNew.base.BaseEntity
import javax.persistence.*

@Entity
data class DeliveryType(

        var type: String? = null,
        var description: String? = null

): BaseEntity()
