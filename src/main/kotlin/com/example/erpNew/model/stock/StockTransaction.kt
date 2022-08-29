package com.example.erpNew.model.stock

import com.example.erpNew.model.item.Item
import com.example.erpNew.base.BaseEntity
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
