package com.example.demo.core.responseFormat.exception.entityExecption

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException(message: String) : RuntimeException(message){
    override val message: String?
        get() = "my bad ${super.message}"
}