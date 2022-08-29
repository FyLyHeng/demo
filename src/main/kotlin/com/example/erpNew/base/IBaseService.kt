package com.example.erpNew.base

import org.springframework.data.domain.Page
import java.util.ArrayList
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

interface IBaseService<T> : IBaseStatusAble<T> {

    fun findById(id: Long): T?
    fun findAll(): List<T>
    fun findAll(allParams: MutableMap<String, String>): MutableList<T>?
    fun listCriteria(allParams: Map<String, String>): Page<T>?


    fun addNew(entity: T): T
    fun updateObj(id: Long, entity: T): T
    fun delete(id: Long)

}

interface IBaseStatusAble<T> {
    fun updateStatus(id : Long) :T
}


/**
 * Bulk processing
 * Insert, Delete, update
 */
interface IBaseMultiProcess<T> {

    fun multiAdd (entities : List<T>)
    fun multiDelete (entities : List<T>)
    fun multiUpdate (entities : List<T>)

}




/**
 * filterable
 * DTO able
 */
interface IBaseQueryable<T> {

    fun addNew(entity: T, customFields: (targetOBJ:T)-> Unit = {}): T
    fun findAll(allParams: MutableMap<String, String>, addOnFilters: (predicates: ArrayList<Predicate>, cb: CriteriaBuilder, root: Root<T>) -> Unit):MutableList<T>?
    fun listCriteria(allParams: Map<String, String>, addOnFilters: (predicates: ArrayList<Predicate>, cb: CriteriaBuilder, root: Root<T>) -> Unit):Page<T>?
}





/**
 * Base validation
 */
interface IBaseValidation<T> {


}







