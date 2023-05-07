package com.example.demo.core

import com.example.demo.core.responseFormat.exception.entityExecption.NotFoundException
import com.example.demo.core.responseFormat.response.JSONFormat
import com.example.demo.core.responseFormat.response.ResponseDTO
import com.example.demo.service.setting.DocumentSettingServiceImpl
import com.example.demo.utilities.AppConstant
import com.example.demo.utilities.UtilService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.lang.reflect.ParameterizedType
import java.util.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import kotlin.collections.ArrayList

abstract class GenericRestfulController<T : BaseEntity>() : DefaultFilter() {

    @Autowired
    lateinit var JSONFormat: JSONFormat

    @Autowired
    protected val repo: BaseRepository<T>? = null

    @Autowired
    lateinit var utilService: UtilService

    @Autowired
    lateinit var documentSettingService: DocumentSettingServiceImpl

    private var isAllowDelete: Boolean? = true
    private var isAllowUpdate: Boolean? = true
    private var isAllowMultiProcess: Boolean? = false
    private var resourceName: String? = null

    constructor(
        allowUpdate: Boolean? = true,
        allowDelete: Boolean? = true,
        allowMultiProcess: Boolean? = false,
    ) : this() {
        this.isAllowDelete = allowDelete
        this.isAllowUpdate = allowUpdate
        this.isAllowMultiProcess = allowMultiProcess
        this.resourceName = this.getGenericTypeClass()?.simpleName
    }


    /**
     * @Base_APIs.
     *
     * base api are fix with endpoint.
     * base api we not allow to override.
     */

    @PostMapping
    open fun create(@RequestBody entity: T): ResponseDTO {

        this.beforeSave(entity)
        val obj = create(entity) {}
        this.afterSaved(obj)
        return JSONFormat.respondID(obj)
    }

    @PostMapping("/multi")
    open fun createMulti(@RequestBody entities: MutableList<T>): ResponseDTO {

        val data = this.createMulti(entities.toList())

        return JSONFormat.respondID(repo?.saveAll(data))
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
    open fun updateMulti(@RequestBody entities: MutableList<T>): ResponseDTO {

        val listOfObj = updateMulti(entities, null, null)

        val obj = repo?.saveAll(listOfObj)
        return JSONFormat.respondID(obj)
    }


    @DeleteMapping("{id}")
    open fun delete(@PathVariable(value = "id") id: Long): ResponseDTO {

        this.checkAllowModify()
        try {
            repo?.deleteById(id)
        } catch (ex: EmptyResultDataAccessException) {
            notFound(id)
        }

        return JSONFormat.respondObj(null, status = HttpStatus.OK)
    }

    @PutMapping("/delete")
    open fun softDelete(@RequestBody listEntity: MutableList<T>): ResponseDTO {

        val listEntityID = listEntity.map { utilService.getValueFromField(it, "id")?.toLong()!! }

        val result = listByIds(listEntityID, true)

        result?.getOrElse(0) { throw NotFoundException("ids doesn't exists") }

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
        val data = allDynamicFilter(allParams) { _, _, _ -> }

        return JSONFormat.respondList(data)

    }


    @GetMapping(AppConstant.LIST_PATH)
    open fun list(@RequestParam allParams: Map<String, String>): ResponseDTO {
        val data = listCriteria(allParams)
        return JSONFormat.respondPage(data)
    }


    @PutMapping(AppConstant.UPDATE_SUBMIT_PATH + "/{id}")
    open fun updateToSubmit(@PathVariable id: Long): ResponseDTO {
        val obj = this.updateStatusToSubmit(id)
        return JSONFormat.respondCustomStatus(repo?.save(obj))
    }

    @PutMapping(AppConstant.UPDATE_CANCEL_PATH + "/{id}")
    open fun updateToCancel(@PathVariable id: Long): ResponseDTO {
        val obj = updateStatusToCancel(id)
        return JSONFormat.respondCustomStatus(repo?.save(obj))
    }

    @PutMapping(AppConstant.UPDATE_STATUS_PATH + "/{id}/{customStatus}")
    fun updateCustomStatus(@PathVariable id: Long, @PathVariable customStatus: String): ResponseDTO {
        val obj = updateStatus(id, FIELD_CUSTOM_STATUS, customStatus)
        return JSONFormat.respondCustomStatus(repo?.save(obj))
    }


//================================================================================================

    /**
     * @Open_override_APIs.
     *
     */

    open fun create(entity: T, customFields: (targetOBJ: T) -> Unit = {}): T {
        customFields(entity)
        return repo?.save(entity)!!
    }

    open fun afterSaved(entity: T) {}
    open fun afterUpdated(entity: T) {}


    @Throws(Exception::class)
    open fun beforeSave(entity: T) {
        validate(entity)
    }

    open fun beforeUpdate(entity: T) {
        validate(entity)
    }


    fun createMulti(entities: List<T>): List<T> {
        entities.forEach {
            validate(it)
            utilService.setValueToField(it, FIELD_CUSTOM_STATUS, DEFAULT_CUSTOMSTATUS)
        }
        return entities
    }

    fun createMulti(entities: List<T>, customFields: (targetObj: T) -> Unit = {}): List<T> {
        entities.forEach {
            validate(it)
            utilService.setValueToField(it, FIELD_CUSTOM_STATUS, DEFAULT_CUSTOMSTATUS)
            customFields(it)
        }
        return entities
    }


    /**
     * Basic Update function using for update Entity base on ID
     * @param id
     */
    fun update(id: Long, entity: T): T {

        val obj = this.getById(id)!!
        utilService.bindProperties(entity, obj)
        return obj
    }


    /**
     *
     * Update function with include and exclude parameter using for update Entity base on ID
     * @param fieldsProtected = list of Entity fields that use for ignore to update those fields
     *
     */
    fun update(id: Long, entity: T, fieldsProtected: List<String>? = null): T {
        val obj = this.getById(id)!!
        utilService.bindProperties(entity, obj, include = null, exclude = fieldsProtected)

        return obj
    }


    /**
     *
     * Update function with fieldsProtected parameter and customFields Lambda block using for update Entity base on ID
     * @param id = is a unit ID of Entity using for find record in DB for update
     * @param fieldsProtected = list of Entity fields that use for ignore to update those fields
     * @param customFields = is a Lambda block for Open to Client to Customize logic with the existing targetObj
     *
     */
    fun update(
        id: Long,
        entity: T,
        fieldsProtected: List<String>? = null,
        customFields: (targetObj: T) -> Unit = {},
    ): T {
        val obj = this.getById(id)!!
        utilService.bindProperties(entity, obj, include = null, exclude = fieldsProtected)
        customFields(obj)
        return obj
    }


    fun updateMulti(entities: MutableList<T>, include: List<String>? = null, exclude: List<String>? = null): List<T> {
        val listOfObj = mutableListOf<T>()
        entities.forEach {
            val id = utilService.getValueFromField(it, "id")?.toLong()!!
            val obj = this.getById(id)!!
            utilService.bindProperties(it, obj, include, exclude)
            listOfObj.add(obj)
        }
        return listOfObj
    }


    fun updateMulti(
        entities: MutableList<T>,
        include: List<String>? = null,
        exclude: List<String>? = null,
        customFields: (targetObj: T) -> Unit = {},
    ): List<T> {
        val listOfObj = mutableListOf<T>()
        entities.forEach {
            val id = utilService.getValueFromField(it, "id")?.toLong()!!
            val obj = this.getById(id)!!
            utilService.bindProperties(it, obj, include, exclude)

            customFields(obj)
            listOfObj.add(obj)
        }
        return listOfObj
    }


    fun updateStatusToSubmit(id: Long): T {

        return updateStatus(id, FIELD_CUSTOM_STATUS, AppConstant.SUBMIT)
    }

    fun updateStatusToSubmit(
        id: Long,
        fieldName: String = FIELD_CUSTOM_STATUS,
        customFields: (targetObj: T) -> Unit = {},
    ): T {

        val obj = updateStatus(id, fieldName, AppConstant.SUBMIT)
        customFields(obj)
        return obj
    }


    fun updateStatusToCancel(id: Long): T {

        return updateStatus(id, FIELD_CUSTOM_STATUS, AppConstant.CANCEL)
    }

    fun updateStatusToCancel(
        id: Long,
        fieldName: String = FIELD_CUSTOM_STATUS,
        customFields: (targetObj: T) -> Unit = {},
    ): T {

        val obj = updateStatus(id, fieldName, AppConstant.CANCEL)
        customFields(obj)
        return obj
    }


    fun updateStatus(id: Long, fieldName: String, customStatus: String): T {
        val obj = getById(id)!!
        utilService.setValueToField(obj, fieldName, customStatus)
        return obj
    }

    fun getById(id: Long): T? {
        return repo?.findById(id)?.orElseThrow { notFound(id) }
    }

    fun listByIds(listEntityID: List<Long>, status: Boolean): MutableList<T>? {
        return repo?.findAllByIdInAndStatus(listEntityID, status)
    }


    fun allDynamicFilter(
        allParams: MutableMap<String, String>,
        addOnFilters: (predicates: ArrayList<Predicate>, cb: CriteriaBuilder, root: Root<T>) -> Unit,
    ): MutableList<T>? {
        val params = DefaultFilter(allParams.toMutableMap())

        return repo?.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()

            addOnFilters(predicates, cb, root)

            cb.and(*predicates.toTypedArray())
        }, Sort.by(params.sortDirection, params.orderBy))
    }


    fun listCriteria(allParams: Map<String, String>): Page<T>? {

        val params = super.applyDefaultPaging(allParams)

        return repo?.findAll({ root, _, cb ->

            val predicates = ArrayList<Predicate>()
            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(params.page, params.size, Sort.by(params.sortDirection, params.orderBy)))
    }

    fun listCriteria(
        allParams: Map<String, String>,
        addOnFilters: (predicates: ArrayList<Predicate>, cb: CriteriaBuilder, root: Root<T>) -> Unit,
    ): Page<T>? {

        val params = super.applyDefaultPaging(allParams)

        return repo?.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()

            addOnFilters(predicates, cb, root)

            cb.and(*predicates.toTypedArray())// * is a function use for convert list or array to Varargs type.
        }, PageRequest.of(params.page, params.size, Sort.by(params.sortDirection, params.orderBy)))
    }


    @GetMapping(AppConstant.LIST_DTO_PATH)

    open fun <R : Any> listCriteriaWithProjection(@RequestParam allParams: Map<String, String>, customDto: Class<R>) : ResponseDTO{
        val data = this.listCriteriaWithProjection(allParams, customDto) { _, _, _ -> }
        return JSONFormat.respondPage(data)
    }

    fun <R : Any> listCriteriaWithProjection(allParams: Map<String, String>, customDto: Class<R>, addOnFilters: (predicates: ArrayList<Predicate>, cb: CriteriaBuilder, root: Root<T>) -> Unit, ): Page<R>? {

        val search = Specification { root: Root<T>, _: CriteriaQuery<*>?, cb: CriteriaBuilder ->

            val predicates = ArrayList<Predicate>()
            addOnFilters(predicates, cb, root)
            cb.and(*predicates.toTypedArray())// * is a function use for convert list or array to Varargs type.
        }


        val defaultPaging = super.applyDefaultPaging(allParams)
        val paging = PageRequest.of(defaultPaging.page, defaultPaging.size, Sort.by(defaultPaging.sortDirection, defaultPaging.orderBy))

        return repo?.findAll(search, customDto, paging)
    }


//================================================================================================


    protected fun notFound(id: Long): NotFoundException {
        throw NotFoundException("$resourceName id $id doesn't exists")
    }

    open fun validate(entity: T?): Boolean {
        //check files type between income data with Model datatype
        return true
    }

    private fun checkAllowModify() {
        when (false) {
            isAllowDelete -> {
                JSONFormat.respondObj(
                    data = null,
                    status = HttpStatus.NOT_ACCEPTABLE,
                    "$resourceName : Not Allow to Delete!"
                )
            }

            isAllowUpdate -> {
                JSONFormat.respondObj(
                    data = null,
                    status = HttpStatus.NOT_ACCEPTABLE,
                    "$resourceName : Not Allow to Update!"
                )
            }

            else -> {}
        }
    }

    private fun checkAllowMultiProcess() {

    }

    @SuppressWarnings("unchecked")
    private fun getGenericTypeClass(): Class<T>? {
        return try {
            val className: String = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0].typeName
            val clazz = Class.forName(className)
            clazz as Class<T>
        } catch (e: java.lang.Exception) {
            throw IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ")
        }
    }

}