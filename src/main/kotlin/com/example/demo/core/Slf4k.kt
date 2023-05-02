package com.example.demo.core

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Slf4k {
    companion object {
        inline var <reified T> T.log: Logger
            get() = LoggerFactory.getLogger(T::class.java)
            set(value) {}
    }
}