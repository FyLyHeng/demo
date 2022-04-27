package com.example.demo.responseFormat.response

import java.lang.reflect.Field


open class ResponseObjectMap {
    val responseObject = ResponseObject()
    fun responseObject(obj: Any?, currentPage: Int, perPage: Int, totalElements: Long): MutableMap<String, Any> {
        val response: MutableMap<String, Any> = HashMap()
        if (obj != null) {
            response["results"] = obj
            response["currentPage"] = currentPage
            response["perPage"] = perPage
            response["length"] = totalElements
            response["response"] = responseObject.success()
        } else {
            response["response"] = responseObject.error()
        }
        return response
    }

    fun responseObject(obj: Any?, totalElements: Long): MutableMap<String, Any> {
        val response: MutableMap<String, Any> = HashMap()
        if (obj != null) {

            response["results"] = obj
            response["length"] = totalElements
            response["response"] = responseObject.success()
        } else {
            response["response"] = responseObject.error()
        }
        return response
    }

    fun responseObject(obj: Any?): MutableMap<String, Any> {
        val response: MutableMap<String, Any> = HashMap()
        if (obj != null) {
            response["results"] = obj

            response["response"] = responseObject.success()
        } else {
            response["response"] = responseObject.error()
        }
        return response
    }

    fun responseObjectID(obj: Any?):MutableMap<String,Any>{
        val response: MutableMap<String, Any> = HashMap()
        val objIDField = "id"

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

        if (obj != null) {

            response["results"] = mapOf(objIDField to getValueFromField(obj, objIDField))

            response["response"] = responseObject.success()
        } else {
            response["response"] = responseObject.error()
        }
        return response
    }

    fun responseCodeWithMessage(code: Int?, message:String): MutableMap<String, Any> {
        val responseObject = ResponseObject(code = code, message = message)
        val response: MutableMap<String, Any> = HashMap()
        response["response"] = responseObject
        return response
    }
}

