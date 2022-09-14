package com.example.demo.core.responseFormat.response

class TreeResponse <T> (
    var data: T? = null,
    var children: List<TreeResponse<T>>? = null
)