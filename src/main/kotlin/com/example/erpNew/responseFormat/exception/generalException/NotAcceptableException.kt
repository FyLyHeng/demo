package com.example.erpNew.responseFormat.exception.generalException

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
class NotAcceptableException(message: String) : RuntimeException(message)