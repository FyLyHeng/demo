package com.example.demo.model.item

import com.example.demo.core.BaseEntity
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne


@Entity
data class Item (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Long?=null,
    var name:String?=null,
    var price:Double?=0.0,
    @OneToOne
    var category : Category?=null
): BaseEntity()