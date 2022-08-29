package com.example.erpNew.utilities

import com.example.erpNew.base.BaseEntity
import org.springframework.beans.BeanUtils
import org.springframework.stereotype.Component
import java.lang.Double.parseDouble
import java.lang.reflect.Field
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import kotlin.reflect.KProperty1

@Component
class UtilService {

    /**
     * @param include: list of properties that will ignore for bind update
     * @param exclude: list of properties that will remove from the existing ignore properties
     *          -> It will allow for bind update
     */
    private fun getIgnoreField(include:List<String>?, exclude:List<String>?): Array<String> {

        //default ignore field
        val baseEntityProperties:MutableList<String> = BaseEntity::class.java.declaredFields.map { it.name } as MutableList<String>
        baseEntityProperties.add("series")
        baseEntityProperties.add("customStatus")

        //add on field
        exclude?.let { baseEntityProperties.removeAll(it) }
        include?.let { baseEntityProperties.addAll(it) }

        return baseEntityProperties.toTypedArray()
    }

    /**
     * @param t: is the new resource for bind
     * @param t1: is the old exist resource
     *
     * @param include are the fields name that allow to update
     * @param exclude are the fields name that not allow updating (ex: id, serial, version, ...)
     *
     *
     * @warning: currently bindProperties not stable for bind boolean type
     */
    fun <T:Any>bindProperties(t: T, t1: T, include:List<String>?=null, exclude:List<String>?=null){
        BeanUtils.copyProperties(t,t1, *getIgnoreField(exclude, include))
    }


    fun <T> filterDateBetween(fieldName:String, startDate: String?, endDate:String?, cb: CriteriaBuilder, root: Root<T>): Predicate? {
        if (startDate != null && endDate != null) {
            val formatter = SimpleDateFormat("yyyy/MM/dd")
            try {
                val start = formatter.parse(startDate)
                val end = formatter.parse(endDate)

                return cb.between(root.get(fieldName),start,end)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * @see : Use this Fun to check does the given parameter value is A Numeric or not
     *
     *
     * @return boolean :
     *      true : stringValue are Number only
     *      false : stringValue not a number (it may content some character or symbol)
     */
    fun checkStringValueIsNumeric(stringValue: String): Boolean {
        var numeric = true

        try {
            val num = parseDouble(stringValue)
        } catch (e: NumberFormatException) {
            numeric = false
        }

        return numeric
    }


    /**
     * @param obj <T>
     * @param targetField : field that wish to access value
     *
     * TODO rename to readInstanceProperty()
     */
    fun getValueFromField(obj: Any, targetField: String) : String? {
        try {
            val field: Field = obj::class.java.getDeclaredField(targetField)
            field.isAccessible = true
            val  values = field.get(obj)
            field.isAccessible = false

            return values.toString()
        }catch (e:Exception){
            //println("GET: ${e.message} NOT EXIST")
        }
        return null
    }

    fun setValueToField(obj: Any, fieldName:String, value:Any) {
        try {
            val field: Field = obj::class.java.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(obj,value)
            field.isAccessible = false
        }catch (e:Exception){
            //println("SET: ${e.message} NOT EXIST")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <R> readInstanceProperty(instance: Any, propertyName: String): R {
        val property = instance::class.members
            .first { it.name == propertyName } as KProperty1<Any, *>
        return property.get(instance) as R
    }

    @Suppress("UNCHECKED_CAST")
    fun getInstanceName(instance: Any): String? {
        return instance::class.simpleName?.lowercase(Locale.getDefault())
    }

    @Suppress("UNCHECKED_CAST")
    fun <T>getInstanceName(instance: Class<T>): String? {
        return instance.simpleName.lowercase(Locale.getDefault())
    }
}
