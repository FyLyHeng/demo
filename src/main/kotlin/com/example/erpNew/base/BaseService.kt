package com.example.erpNew.base

import com.example.erpNew.responseFormat.exception.entityExecption.NotFoundException
import com.example.erpNew.responseFormat.response.JSONFormat
import com.example.erpNew.utilities.UtilService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.ArrayList
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@Component
class BaseService<T : BaseEntity> : IBaseService<T>, IBaseQueryable<T>{

    @Autowired
    val repo: BaseRepository<T>? = null

    @Autowired
    lateinit var utilService: UtilService


    override fun findById(id: Long): T? {
        return repo?.findById(id)?.orElseThrow { notFound(id) }
    }

    override fun addNew(entity: T): T {
        return repo!!.save(entity)
    }

    override fun addNew(entity: T, customFields: (targetOBJ:T)-> Unit): T {
        customFields(entity)
        return repo!!.save(entity)
    }

    override fun delete(id:Long){
        this.checkAllowModify()
        try {
            repo?.deleteById(id)
        } catch (ex: EmptyResultDataAccessException){
            notFound(id)
        }
    }

    override fun updateObj(id: Long, entity: T): T {

        val obj= this.get(id)!!
        utilService.bindProperties(entity, obj)
        return obj
    }

    fun update(id: Long, entity: T, include: List<String>? = null, exclude: List<String>? = null): T {
        val obj= this.get(id)!!
        utilService.bindProperties(entity, obj, include = include, exclude = exclude)

        return obj
    }

    fun update(id: Long, entity: T, include: List<String>? = null, exclude: List<String>? = null, customChild: (targetObj: T) -> Unit = {}): T {
        val obj = this.get(id)!!
        utilService.bindProperties(entity, obj, include = include, exclude = exclude)
        customChild(obj)
        return obj
    }

    override fun findAll(): List<T> {
        return repo!!.findAll()
    }

    override fun listCriteria(allParams: Map<String, String>): Page<T>? {
        val params = DefaultFilter(allParams.toMutableMap())


        return repo?.findAll({ root, _, cb ->

            val predicates = ArrayList<Predicate>()
            this.defaultFilterFields(predicates, cb, root, allParams)

            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(params.page, params.size, Sort.by(params.sortDirection, params.orderBy)))
    }
    override fun listCriteria(allParams: Map<String, String>, addOnFilters: (predicates: ArrayList<Predicate>, cb: CriteriaBuilder, root: Root<T>) -> Unit):Page<T>? {

        val params = DefaultFilter(allParams.toMutableMap())


        return repo?.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()

            defaultFilterFields(predicates,cb, root, allParams)
            addOnFilters(predicates, cb, root)

            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(params.page, params.size, Sort.by(params.sortDirection, params.orderBy)))
    }


    override fun findAll(allParams: MutableMap<String, String>): MutableList<T>? {
        val params = DefaultFilter(allParams.toMutableMap())

        return repo?.findAll ({ root, _, cb ->
            val predicates = ArrayList<Predicate>()

            this.defaultFilterFields(predicates,cb,root, allParams)
            cb.and(*predicates.toTypedArray())

        }, Sort.by(params.sortDirection,params.orderBy))
    }
    override fun findAll(allParams: MutableMap<String, String>,addOnFilters: (predicates: ArrayList<Predicate>, cb: CriteriaBuilder, root: Root<T>) -> Unit): MutableList<T>? {
        val params = DefaultFilter(allParams.toMutableMap())

        return repo?.findAll ({ root, _, cb ->
            val predicates = ArrayList<Predicate>()

            defaultFilterFields(predicates,cb,root, allParams)
            addOnFilters(predicates,cb,root)

            cb.and(*predicates.toTypedArray())
        }, Sort.by(params.sortDirection,params.orderBy))
    }




    open fun afterSaved(entity: T){}
    open fun afterUpdated(entity: T){}



    @Throws(Exception::class)
    open fun beforeSave(entity:T) {
        validateFields(entity)
    }
    open fun beforeUpdate(entity:T) {
        validateFields(entity)
    }




    //step 1 : split listData to [listOfExistId, listOfNotExistId]
    //step 2 : Do-update for listExist
    //step 3 : Log for listNotExistId
    open fun updateMulti(entities: MutableList<T>, include: List<String>? = null, exclude: List<String>? = null): List<T>{
        val listOfObj = mutableListOf<T>()
        entities.forEach {
            val id = utilService.getValueFromField(it, "id")?.toLong()!!
            val obj = this.get(id)!!
            utilService.bindProperties(it, obj, include, exclude)
            listOfObj.add(obj)
        }
        return listOfObj
    }
    fun updateMulti(entities: MutableList<T>, include: List<String>? = null, exclude: List<String>? = null, customChild: (targetObj: T) -> Unit = {}): List<T>{
        val listOfObj = mutableListOf<T>()
        entities.forEach {
            val id = utilService.getValueFromField(it, "id")?.toLong()!!
            val obj = this.get(id)!!
            utilService.bindProperties(it, obj, include, exclude)

            customChild(obj)
            listOfObj.add(obj)
        }
        return listOfObj
    }


    open fun updateStatus(id: Long, customStatus: String): T {
        val obj = findById(id)!!
        utilService.setValueToField(obj,"customStatus",customStatus)
        return obj
    }

//================================================================================================

    protected fun notFound (id: Long) : NotFoundException {
        //TODO retrive id without passing vai fun parameter.
        //val ID = utilService.readInstanceProperty<T>(resource.classes,"id")
        throw NotFoundException("$resourceName id $id doesn't exists")
    }

    private fun validateFields(entity: T?) : Boolean {
        //entity::class.java.declaredFields
        return true
    }

    private fun <T> defaultFilterFields(predicates: ArrayList<Predicate>, cb: CriteriaBuilder, root: Root<T>, defaultFilterFields: Map<String, String?>) {

        val SERIES = "series"
        val ID = "id"
        val STATUS = "status"

        defaultFilterFields.forEach { (fieldName, value) ->

            when (fieldName){
                SERIES              -> predicates.add(cb.like(cb.upper(root.get(fieldName)), "%${value.toString().toUpperCase()}%"))
                ID                  -> predicates.add(cb.equal(root.get<Long>(fieldName), value))
            }
        }
        predicates.add(cb.isTrue(root.get(STATUS)))
    }

    private fun checkAllowModify(){
        when (false){
            isAllowUpdate -> {JSONFormat.respondObj(data = null, status = HttpStatus.NOT_ACCEPTABLE,"Update Method Is Not Allow!")}
        }
        when (false){
            isAllowDelete -> {JSONFormat.respondObj(data = null, status = HttpStatus.NOT_ACCEPTABLE,"Delete Method Is Not Allow!")}
        }
    }

    override fun updateStatus(id: Long): T {
        TODO("Not yet implemented")
    }

}