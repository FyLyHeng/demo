package com.example.demo.core.responseFormat.response

import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK

interface ResponseFormat {

    fun respondID(data: Any?, status: HttpStatus?=OK, message: String?=""): ResponseDTO

    fun respondCustomStatus(data: Any?, status: HttpStatus?=OK, message: String?=""): ResponseDTO

    fun respondObj(data: Any?, status: HttpStatus?=OK, message: String?=""): ResponseDTO

    fun respondList(data: List<Any>?, status: HttpStatus?=OK, message: String?=""): ResponseDTO

    fun <T: Any> respondPage(data: Page<T>?, status: HttpStatus?=OK, message: String?=""): ResponseDTO

    fun respondDynamic(data: Any?, status: HttpStatus?=OK, message: String?="", total: Long): ResponseDTO

}