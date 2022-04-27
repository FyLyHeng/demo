package com.example.demo.responseFormat.exception

class CustomException(status: Int, message: String) : RuntimeException(message) {
    var status: Int = 0
    init {
        this.status = status
    }
}