package com.example.demo.service.setting

import com.example.demo.model.setting.DocumentSetting
import com.example.demo.repository.DocumentSettingRepository
import com.example.demo.utilities.UtilService
import com.example.demo.responseFormat.exception.CustomNotAcceptableException
import com.example.demo.responseFormat.exception.CustomNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import javax.persistence.criteria.Predicate
import javax.transaction.Transactional

@Service
class DocumentSettingServiceImpl : DocumentSettingService {

    @Autowired
    lateinit var documentSettingRepository: DocumentSettingRepository
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate
    @Autowired
    lateinit var utilService : UtilService



    override fun findAllList(q: String?, page: Int, size: Int): Page<DocumentSetting>? {
        return documentSettingRepository.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()
            if (q != null) {
                val name = cb.like(cb.upper(root.get("name")), "%${q.toUpperCase()}%")
                predicates.add(name)
            }
            predicates.add(cb.isTrue(root.get<Boolean>("status")))
            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }

    override fun findById(id: Long): DocumentSetting? {
        return documentSettingRepository.findByIdAndStatusTrue(id)
                ?: throw CustomNotFoundException("Series id $id does not exist")
    }

    override fun addNew(t: DocumentSetting): DocumentSetting? {
        checkExceptions(t)
        return documentSettingRepository.save(t)
    }

    override fun updateObj(id: Long, t: DocumentSetting): DocumentSetting? {
        val series = findById(id)!!
        checkExceptions(t)
        utilService.bindProperties(t, series)
        t.isDifferentPrefix?.let { series.isDifferentPrefix = it }
        t.isDifferentSequence?.let { series.isDifferentSequence = it }

        documentSettingRepository.save(series)
        return series
    }

    override fun findAll(): List<DocumentSetting>? = documentSettingRepository.findAllByStatusTrueOrderByIdDesc()

    fun checkExceptions(documentSetting: DocumentSetting) {
        documentSetting.name ?: throw CustomNotAcceptableException("Field name is required")
        documentSetting.prefix ?: throw CustomNotAcceptableException("Field prefix is required")
    }

    @Transactional
    fun getNextSeries(name:String, isIncludeVat:Boolean=false): String {

        val documentSetting = documentSettingRepository.findByName(name) ?: throw CustomNotFoundException("$name next series not found.")
        var prefix : String ?=null
        var lastCode :Int ?= null


        if (isIncludeVat){
            prefix = documentSetting.prefix
            lastCode = documentSetting.lastCode!!+1
            documentSettingRepository.updateLastCode()
        }

        else{
            prefix = if (documentSetting.isDifferentPrefix == true){
                documentSetting.nonVatPrefix
            }else {
                documentSetting.prefix
            }


            lastCode = if (documentSetting.isDifferentSequence == true){
                documentSettingRepository.updateNonVatLastCode()
                documentSetting.nonVatLastCode!!+1
            }else {
                documentSettingRepository.updateLastCode()
                documentSetting.lastCode!!+1
            }
        }

        return seriesFormat(lastCode,prefix, documentSetting.length)

    }

    /**
     * @Using  to get Next code for target Module (Ex: invoice_no, sale_order_series, ...)
     * @param name of the DocumentSetting name (like: invoice, saleOrder, item, ...)
     *
     * @Do
     *      - update last_code +1
     *      - and return hold Model of DocumentSetting (in only one transaction)
     *
     * @throws CustomNotFoundException in case the given name not exist in DocumentSetting table.
     *
     * @return seriesFormat (Prefix + LastCode) ex:(INV-00001, SO-00002, ...)
     */
    @Transactional
    fun getNextSeries (name: String) : String{
        val series = jdbcTemplate.queryForObject("UPDATE document_setting SET last_code = last_code+1 " +
                                                      "WHERE name ='$name' RETURNING id,name, suffix, prefix, last_code, description",

            BeanPropertyRowMapper(DocumentSetting::class.java))
            ?: throw CustomNotFoundException("$name next series not found.")

        return seriesFormat(series.lastCode,series.prefix, series.length)
    }


    fun getCurrentSeries(name: String): String {
        val series = documentSettingRepository.findByName(name)!!
        return seriesFormat(series.lastCode,series.prefix,  series.length)
    }

    fun getByName(name: String): DocumentSetting? {
        return documentSettingRepository.findByName(name)
    }


    private fun seriesFormat(lastCode:Int?, prefix:String?, length:Int?): String {
        return "$prefix-${String.format("%0${length.toString()}d", lastCode)}"
    }

    private fun nextCodeVat (prefix: String?, lastCode: Int?, documentSetting: DocumentSetting){
    }

    private fun nextCodeNonVat (prefix: String?, lastCode: Int?, documentSetting: DocumentSetting){
    }


}
