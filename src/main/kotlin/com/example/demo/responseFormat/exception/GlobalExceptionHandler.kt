package com.example.demo.responseFormat.exception

import com.example.demo.responseFormat.exception.entityExecption.NotFoundException
import com.example.demo.responseFormat.exception.generalException.NotAcceptableException
import com.example.demo.responseFormat.response.ResponseDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.*

/**
 * @exception
 *
 *
 *
 */

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class GlobalExceptionHandler: ResponseEntityExceptionHandler() {

    @Autowired
    var ResponseDTO = ResponseDTO()

    /**
     *
     */
    @ExceptionHandler(Exception::class)
    fun handleAllExceptions( ex: Exception,  request:WebRequest) : ResponseEntity<Any>{
        val message = ex.message?:"Unexpected Error"
        val status = HttpStatus.INTERNAL_SERVER_ERROR

        val body = ResponseDTO.apply {
            this.data = null
            this.code = status.value()
            this.message = message
            this.error = status.reasonPhrase
            this.timestamp = Date()
        }
        this.logger.error(message)
        return ResponseEntity(body,status)
    }


    /**
     * @override handleMethodArgumentNotValid
     *
     * @USING: For Handle the Client Request with wrong format or datatype fields Json (ex: Double price = "some text")
     *
     *
     * @return RespondDTO Format
     *      message : default header HttpStatus
     *      code : default header HttpStatus
     *      error : list of Errors Fields with error cause message
     */
    override fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {

        val errors: MutableList<String> = ArrayList()
        for (error in ex.bindingResult.fieldErrors) {
            errors.add(error.field + ": " + error.defaultMessage)
        }

        for (error in ex.bindingResult.globalErrors) {
            errors.add(error.objectName + ": " + error.defaultMessage)
        }

        val body = ResponseDTO.apply {
            this.data = null
            this.code = status.value()
            this.message = status.reasonPhrase
            this.error = errors
            this.timestamp = Date()
        }

        this.logger.error(errors)
        return ResponseEntity(body, headers, status)
    }


    /**
     * @override fun handleHttpMessageNotReadable
     *
     * @USING: For handle RespondError in case Client Pass Invalid Data to Server
     *      HttpStatus : BAD_REQUEST
     *
     * @return RespondDTO Format (with Header status BAD_REQUEST)
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    override fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {

        val errors = ex.cause?.message?:"Unexpected Error : JSON parse "

        val body = ResponseDTO.apply {
            this.data = null
            this.code = status.value()
            this.message = status.reasonPhrase
            this.error = errors
            this.timestamp = Date()
        }

        this.logger.error(errors)
        return ResponseEntity(body, headers, status)
    }


    /**
     * @override fun handleHttpMessageNotWritable
     *
     * @USING: For handle RespondError in case Server Respond Invalid Data as Entity Body
     *      HttpStatus : BAD_REQUEST
     *
     * @return RespondDTO Format (with Header status INTERNAL_SERVER_ERROR)
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    override fun handleHttpMessageNotWritable(ex: HttpMessageNotWritableException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {

        val errors = ex.cause?.message?:"Unexpected Error : JSON parse "
        val body = ResponseDTO.apply {
            this.data = null
            this.code = status.value()
            this.message = status.reasonPhrase
            this.error = errors
            this.timestamp = Date()
        }

        this.logger.error(errors)
        return ResponseEntity(body, headers, status)
    }


    /**
     * @override NotFoundException
     *
     * @return RespondDTO Format (with Header status NOT_FOUND)
     */
    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected fun handleEntityNotFounds(ex: NotFoundException?,request: WebRequest): ResponseEntity<Any> {
        val errors = ex?.message?:"Unexpected Error"
        val status = HttpStatus.NOT_FOUND

        val body = ResponseDTO.apply {
            this.data = null
            this.code = status.value()
            this.message = errors
            this.error = null
            this.timestamp = Date()
        }
        this.logger.error(errors)
        return ResponseEntity(body, status)
    }


    @ExceptionHandler(NotAcceptableException::class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    protected fun handleNotAcceptable(ex: NotFoundException?,request: WebRequest): ResponseEntity<ResponseDTO> {
        val errors = ex?.message?:"Unexpected Error"
        val status = HttpStatus.NOT_ACCEPTABLE

        val body = ResponseDTO.apply {
            this.data = null
            this.code = status.value()
            this.message = errors
            this.error = status.reasonPhrase
            this.timestamp = Date()
        }
        this.logger.error(errors)
        return ResponseEntity(body, status)
    }
}