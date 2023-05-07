package com.example.demo.core

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.*

open class DefaultFilter() {

    /**
     * Default field Name
     */
    val ID = "id"
    val ORDER_BY = "orderBy"
    val SORT = "sort"
    val PAGE = "page"
    val SIZE = "size"
    val SERIES = "series"
    val STATUS = "status"
    val FIELD_CUSTOM_STATUS = "customStatus"


    /**
     * Default Value
     */
    val DEFAULT_CUSTOMSTATUS = "DRAFT"


    /**
     * Default Filter
     */
    val defaultPage = 0
    val defaultSize = 10
    val defaultOrderBy = "id"
    val defaultSort = "DESC"

    var page = 0
    var size = 0
    var orderBy = ""
    var sort = ""
    var sortDirection = Sort.Direction.DESC

    constructor(allParams:MutableMap<String,String>):this(){

        this.page = allParams[PAGE]?.toInt() ?: defaultPage
        this.size = allParams[SIZE]?.toInt() ?: defaultSize
        this.orderBy = allParams[ORDER_BY] ?: defaultOrderBy
        this.sort = allParams[SORT]?.uppercase(Locale.getDefault()) ?:defaultSort
        this.sortDirection = Sort.Direction.valueOf(sort)
    }

    fun applyDefaultPaging(allParams: Map<String, String>): DefaultFilter {
        this.page = allParams[PAGE]?.toInt() ?: defaultPage
        this.size = allParams[SIZE]?.toInt() ?: defaultSize
        this.orderBy = allParams[ORDER_BY] ?: defaultOrderBy
        this.sort = allParams[SORT]?.uppercase(Locale.getDefault()) ?:defaultSort
        this.sortDirection = Sort.Direction.valueOf(sort)
        return this
    }

    fun getDefaultPaging() : DefaultFilter {
        return this
    }
}
