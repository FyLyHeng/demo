package com.example.erpNew.responseFormat.response

import org.springframework.stereotype.Component
import java.util.*

@Component
class ResponseDTO(

    var code: Int?,
    var message: String?,
    var timestamp: Date?,
    var data: Any?=null,
    var total: Long? = null,
    var error: Any? = null
) {

    constructor() : this(data=null, code=null, message= null, timestamp=null)

    init {
        println("init ${this.toString()}")
    }

    fun finalize (){
        println("destroy ${this.toString()}")
    }

}