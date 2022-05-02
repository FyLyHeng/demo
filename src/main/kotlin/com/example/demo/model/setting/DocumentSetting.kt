package com.example.demo.model.setting

import com.example.demo.base.BaseEntity
import javax.persistence.*

@Entity
@Table(indexes = [Index(columnList = "name")])
data class DocumentSetting(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = 0,

        @Column(unique = true)
        var name: String? = null,
        var length: Int? = 5,
        var description: String? = null,

        @Column(unique = true)
        var prefix:String?=null,
        var suffix:String?=null,
        var lastCode:Int? = 0,


        var isDifferentPrefix : Boolean ?= false,
        var isDifferentSequence : Boolean ?= false,

        @Column(unique = true)
        var nonVatPrefix:String?=null,
        var nonVatSuffix:String?=null,
        var nonVatLastCode:Int?= 0
) : BaseEntity() {
    constructor(id: Long) : this(id, null)
}
