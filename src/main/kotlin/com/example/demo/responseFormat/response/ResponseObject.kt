package com.example.demo.responseFormat.response

class ResponseObject(var code : Int ?= 0, var message: String ?= null) {
    fun success(): ResponseObject = ResponseObject(200, "Success")
    fun error(): ResponseObject = ResponseObject(404, "Error object not found")
}