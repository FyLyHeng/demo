package com.example.demo.core.responseFormat.response

import org.springframework.http.HttpStatus

enum class StatusCode {

    /*OK(1 ,"Success" ),


    //-- warning block
    Invalid(10001, "Invalid data , please check data validation"),
    RecordNotFound(1002 , "Record not found"),
    FORBIDDEN (403, "Forbidden, Unauthorized"),
    INVALIDTOKEN(498,"Invalid Token"),


    //-- Error
    InternalServerError(10500 , "Internal Server Error"),
    *//**
     * Unauthorized Error by Third-party HTTP Error Code 4xx
     *//*
    UnauthorizedErrorByThirdPartyError4xx(10501 , "Unauthorized by Third-party"),
    *//**
     * Error from Third Party HTTP Error Code 5xx
     *//*
    BadRequestErrorByThirdPartyError5xx (10502 , "Bad Request Error by Third-party"),

    *//***
     * Redis Error Code 10600 10650
     *//*
    *//**
     * Redis : Error bad request key and value
     *//*
    RedisBadRequestKeyValue (10600 , "Bad Request Key or Value"),

    *//**
     * Redis : Error bad request with key and index
     *//*
    RedisBadRequestKeyIndex (10601 , "Bad Request Key or Index"),

    *//**
     * Redis : Error bad request with key and subKey and value
     *//*
    RedisBadRequestKeySubKeyValue (10602 , "Bad Request Key or subKey or Value"),

    *//**
     * Redis : Error bad request with key or chanelName
     *//*
    RedisBadRequestKey (10603 , "Bad Request Key or chanelName"),
    ForceMobileUpdate(19999,"The app is outdated. Please, update it to the latest version")*/

}