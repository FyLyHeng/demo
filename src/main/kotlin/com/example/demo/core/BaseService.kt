package com.ig.erp.base

import org.springframework.data.domain.Page

interface BaseService<T> {
    fun findAllList(q: String?, page: Int, size: Int): Page<T>?
    fun findById(id: Long): T?
    fun addNew(t: T): T?
    fun updateObj(id: Long, t: T): T?
    fun findAll(): List<T>?
}