package com.example.demo.responseFormat.response


data class ObjectResponseList(
        var response : Response?= null,
        var results : List<Any> ?= null
)