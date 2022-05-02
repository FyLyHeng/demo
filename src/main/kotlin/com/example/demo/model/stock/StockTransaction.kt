package com.example.demo.model.stock

import com.example.demo.model.item.Item
import com.example.demo.base.BaseEntity
import java.util.Date
import javax.persistence.*

@Entity
data class StockTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Long?=null,
    var qty:Int?=0,
    var date: Date?=Date(),
    var referenceNo:String?,
    @OneToOne(fetch = FetchType.LAZY)
    var item: Item,
): BaseEntity()
