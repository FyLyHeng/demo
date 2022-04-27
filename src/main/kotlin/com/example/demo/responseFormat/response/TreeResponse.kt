package com.example.demo.responseFormat.response

class TreeResponse <T> (
    var data: T? = null,
    var children: List<TreeResponse<T>>? = null
)