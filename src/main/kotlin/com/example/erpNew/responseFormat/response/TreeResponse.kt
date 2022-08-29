package com.example.erpNew.responseFormat.response

class TreeResponse <T> (
    var data: T? = null,
    var children: List<TreeResponse<T>>? = null
)