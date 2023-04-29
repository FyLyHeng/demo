package com.example.demo.core

import com.example.demo.core.responseFormat.exception.entityExecption.NotFoundException
import com.example.demo.service.setting.DocumentSettingServiceImpl
import com.example.demo.utilities.AppConstant
import com.example.demo.utilities.UtilService
import com.example.demo.core.responseFormat.response.JSONFormat
import com.example.demo.core.responseFormat.response.ResponseDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

abstract class GenericRestfulController<T : BaseEntity>(resource: Class<T>) : DefaultFilter() {

    @Autowired
    lateinit var JSONFormat : JSONFormat
    @Autowired
    protected val repo: BaseRepository<T>? = null
    @Autowired
    lateinit var utilService: UtilService
    @Autowired
    lateinit var documentSettingService: DocumentSettingServiceImpl


    constructor(resource: Class<T>,allowUpdate: Boolean?=true, allowDelete:Boolean?=true, allowMultiProcess:Boolean?=false) : this(resource) {
        this.isAllowDelete = allowDelete
        this.isAllowUpdate = allowUpdate
        this.isAllowMultiProcess = allowMultiProcess
    }

    private var isAllowDelete : Boolean?=true
    private var isAllowUpdate : Boolean?=true
    private var isAllowMultiProcess : Boolean?=false
    private var resourceName: String? = resource.javaClass.simpleName



    /**
     * @Base_APIs.
     *
     * base api are fix with endpoint.
     * base api we not allow to override.
     */

    @PostMapping
    open fun create(@RequestBody entity: T): ResponseDTO {

        this.beforeSave(entity)

        val obj = repo?.save(create(entity) {})!!

        this.afterSaved(obj)
        return JSONFormat.respondID(obj)
    }

    @PostMapping("/multi")
    open fun createMulti(@RequestBody entities: MutableList<T>): ResponseDTO {

        entities.forEach {
            validateFields(it)
            utilService.setValueToField(it, FIELD_CUSTOM_STATUS,DEFAULT_CUSTOMSTATUS)
        }


        return JSONFormat.respondID(repo?.saveAll(entities))
    }


    @PutMapping("{id}")
    open fun update(@PathVariable(value = "id") id: Long, @RequestBody entity: T?): ResponseDTO {

        this.beforeUpdate(entity!!)

        val data = repo?.save(
            update(id, entity)
        )!!

        this.afterUpdated(data)

        return JSONFormat.respondID(data)
    }

    @PutMapping("/multi")
    open fun updateMulti( @RequestBody entities: MutableList<T>): ResponseDTO {

        val listOfObj = updateMulti(entities, null, null)

        val obj = repo?.saveAll(listOfObj)
        return JSONFormat.respondID(obj)
    }


    @DeleteMapping("{id}")
    open fun delete(@PathVariable(value = "id") id: Long): ResponseDTO {

        this.checkAllowModify()
        try {
            repo?.deleteById(id)
        } catch (ex: EmptyResultDataAccessException){
            notFound(id)
        }

        return JSONFormat.respondObj(null, status = HttpStatus.OK)
    }

    @PutMapping("/delete")
    open fun softDelete(@RequestBody listEntity: MutableList<T>) : ResponseDTO {

        val listEntityID = listEntity.map { utilService.getValueFromField(it,"id")?.toLong()!! }

        val result = listByIds(listEntityID, true)

        result?.getOrElse(0){ throw NotFoundException( "ids doesn't exists") }

        result?.forEach { it.status = false }
        val data = repo!!.saveAll(result!!)

        return JSONFormat.respondObj(data)
    }


    @GetMapping("{id}")
    open fun get(@PathVariable(value = "id") id: Long): ResponseDTO {
        return JSONFormat.respondObj(getById(id))
    }


    @GetMapping(AppConstant.ALL_PATH)
    open fun all(@RequestParam allParams: MutableMap<String, String>): ResponseDTO {

        val params = DefaultFilter(allParams.toMutableMap())

        val data = repo?.findAll ({ root, _, cb ->
            val predicates = ArrayList<Predicate>()

            this.defaultFilterFields(predicates,cb,root, allParams)
            cb.and(*predicates.toTypedArray())

        }, Sort.by(params.sortDirection,params.orderBy))

        return JSONFormat.respondList(data)

    }


    @GetMapping(AppConstant.LIST_PATH)
    open fun list(@RequestParam allParams: Map<String, String>): ResponseDTO {
        return JSONFormat.respondPage(listCriteria(allParams))
    }


    @PutMapping(AppConstant.UPDATE_SUBMIT_PATH +"/{id}")
    open fun updateToSubmit(@PathVariable id: Long): ResponseDTO {
        val obj = this.updateStatusToSubmit(id)
        return JSONFormat.respondCustomStatus(repo?.save(obj))
    }


    @PutMapping(AppConstant.UPDATE_CANCEL_PATH +"/{id}")
    open fun updateToCancel(@PathVariable id: Long): ResponseDTO {
        val obj = updateStatusToCancel(id)
        return JSONFormat.respondCustomStatus(repo?.save(obj))
    }

    @PutMapping(AppConstant.UPDATE_STATUS_PATH +"/{id}/{customStatus}")
    fun updateCustomStatus(@PathVariable id : Long, @PathVariable customStatus : String) : ResponseDTO {
        val obj = updateStatus(id,customStatus)
        return JSONFormat.respondCustomStatus(repo?.save(obj))
    }



//================================================================================================

    /**
     * @Open_override_APIs.
     *
     */

    open fun create(entity: T, customFields: (targetOBJ:T)-> Unit = {}):T{
        customFields(entity)
        return entity
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


    fun createMulti (entities : List<T>) : List<T> {
        entities.forEach {
            validateFields(it)
            utilService.setValueToField(it, FIELD_CUSTOM_STATUS,DEFAULT_CUSTOMSTATUS)
        }
        return entities
    }

    fun createMulti (entities : List<T>, customFields: (targetObj: T) -> Unit = {}) : List<T> {
        entities.forEach {
            validateFields(it)
            utilService.setValueToField(it, FIELD_CUSTOM_STATUS,DEFAULT_CUSTOMSTATUS)
            customFields(it)
        }
        return entities
    }


    fun update(id: Long, entity: T): T {

        val obj= this.getById(id)!!
        utilService.bindProperties(entity, obj)
        return obj
    }

    fun update(id: Long, entity: T, include: List<String>? = null, exclude: List<String>? = null): T {
        val obj= this.getById(id)!!
        utilService.bindProperties(entity, obj, include = include, exclude = exclude)

        return obj
    }
    fun update(id: Long, entity: T, include: List<String>? = null, exclude: List<String>? = null, customChild: (targetObj: T) -> Unit = {}): T {
        val obj = this.getById(id)!!
        utilService.bindProperties(entity, obj, include = include, exclude = exclude)
        customChild(obj)
        return obj
    }


    fun updateMulti(entities: MutableList<T>, include: List<String>? = null, exclude: List<String>? = null): List<T>{
        val listOfObj = mutableListOf<T>()
        entities.forEach {
            val id = utilService.getValueFromField(it, "id")?.toLong()!!
            val obj = this.getById(id)!!
            utilService.bindProperties(it, obj, include, exclude)
            listOfObj.add(obj)
        }
        return listOfObj
    }

    fun updateMulti(entities: MutableList<T>, include: List<String>? = null, exclude: List<String>? = null, customChild: (targetObj: T) -> Unit = {}): List<T>{
        val listOfObj = mutableListOf<T>()
        entities.forEach {
            val id = utilService.getValueFromField(it, "id")?.toLong()!!
            val obj = this.getById(id)!!
            utilService.bindProperties(it, obj, include, exclude)

            customChild(obj)
            listOfObj.add(obj)
        }
        return listOfObj
    }


    fun updateStatusToSubmit(id: Long): T {
        val obj = getById(id)!!
        utilService.setValueToField(obj,FIELD_CUSTOM_STATUS,AppConstant.SUBMIT)
        return obj
    }
    fun updateStatusToSubmit(id: Long, fieldName: String = FIELD_CUSTOM_STATUS, postGL: (targetObj: T) -> Unit = {}): T {
        val obj = getById(id)!!
        utilService.setValueToField(obj,fieldName,AppConstant.SUBMIT)
        postGL(obj)
        return obj
    }


    fun updateStatusToCancel(id: Long): T {
        val obj = getById(id)!!
        utilService.setValueToField(obj,FIELD_CUSTOM_STATUS,AppConstant.CANCEL)
        return obj
    }
    fun updateStatusToCancel(id: Long, fieldName: String = FIELD_CUSTOM_STATUS, postGL: (targetObj: T) -> Unit = {}): T {
        val obj = getById(id)!!
        utilService.setValueToField(obj,fieldName,AppConstant.CANCEL)
        postGL(obj)
        return obj
    }

    fun updateStatus(id: Long, customStatus: String): T {
        val obj = getById(id)!!
        utilService.setValueToField(obj,FIELD_CUSTOM_STATUS,customStatus)
        return obj
    }



    fun getById(id: Long): T? {
        return repo?.findById(id)?.orElseThrow { notFound(id) }
    }

    fun listByIds(listEntityID : List<Long>, status:Boolean): MutableList<T>? {
        return repo?.findAllByIdInAndStatus(listEntityID,status)
    }

    fun allDynamicFilter(allParams: MutableMap<String, String>,addOnFilters: (predicates: ArrayList<Predicate>, cb: CriteriaBuilder, root: Root<T>) -> Unit): MutableList<T>? {
        val params = DefaultFilter(allParams.toMutableMap())

        return repo?.findAll ({ root, _, cb ->
            val predicates = ArrayList<Predicate>()

            defaultFilterFields(predicates,cb,root, allParams)
            addOnFilters(predicates,cb,root)

            cb.and(*predicates.toTypedArray())
        }, Sort.by(params.sortDirection,params.orderBy))
    }


    fun listCriteria(allParams: Map<String, String>): Page<T>? {
        val params = DefaultFilter(allParams.toMutableMap())


        return repo?.findAll({ root, _, cb ->

            val predicates = ArrayList<Predicate>()
            this.defaultFilterFields(predicates, cb, root, allParams)

            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(params.page, params.size, Sort.by(params.sortDirection, params.orderBy)))
    }
    fun listCriteria(allParams: Map<String, String>, addOnFilters: (predicates: ArrayList<Predicate>, cb: CriteriaBuilder, root: Root<T>) -> Unit):Page<T>? {

        val params = DefaultFilter(allParams.toMutableMap())


        return repo?.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()

            defaultFilterFields(predicates,cb, root, allParams)
            addOnFilters(predicates, cb, root)

            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(params.page, params.size, Sort.by(params.sortDirection, params.orderBy)))
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
            isAllowDelete -> {JSONFormat.respondObj(data = null, status = HttpStatus.NOT_ACCEPTABLE,"Delete Method Is Not Allow!")}
            isAllowUpdate -> {JSONFormat.respondObj(data = null, status = HttpStatus.NOT_ACCEPTABLE,"Update Method Is Not Allow!")}
            else -> {}
        }
    }

    private fun checkAllowMultiProcess(){

    }
}