package com.example.demo.model.item

import com.example.demo.core.BaseEntity
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Category (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Long?=null,
    var name:String?=null
): BaseEntity()