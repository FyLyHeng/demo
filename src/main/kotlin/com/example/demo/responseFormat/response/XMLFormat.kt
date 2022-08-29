package com.example.demo.responseFormat.response

import com.example.demo.utilities.UtilService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.*

@Component
class XMLFormat : ResponseFormat {
    @Autowired
    lateinit var utilService: UtilService
    @Autowired
    lateinit var ResponseDTO : ResponseDTO

    init {
        println("init ${this.toString()}")
    }
    fun finalize (){
        println("destroy ${this.toString()}")
    }

    //Static Member
    companion object {
        private val OK = HttpStatus.OK
    }

    /**
     * Return Single Object ID
     * Most use in Create & Update Obj APIs
     */
    override fun respondID(data: Any?, status: HttpStatus?, message: String?): ResponseDTO {
        return ResponseDTO.apply {
            this.data = mapOf("id" to utilService.getValueFromField(data!!, "id"))
            this.code = (status?.value() ?: OK.value())
            this.message = message ?: status?.reasonPhrase!!
            this.total = 1
            this.error =null
            this.timestamp = Date()
        }
    }


    /**
     * Return customStatus field of Base Entity
     * Most Use in Update Status Cancel_Complete
     */
    override fun respondCustomStatus(data: Any?, status: HttpStatus?, message: String?): ResponseDTO {
        return ResponseDTO.apply {
            this.data = mapOf("customStatus" to utilService.getValueFromField(data!!, "customStatus"))
            this.code = (status?.value() ?: OK.value())
            this.message = message ?: status?.reasonPhrase
            this.total = 1
            this.error = null
            this.timestamp = Date()

        }
    }


    /**
     * Return Single Object
     */
    override fun respondObj(data: Any?, status: HttpStatus?, message: String?): ResponseDTO {

        return ResponseDTO.apply {
            this.data = data
            this.code = (status?.value() ?: OK.value())
            this.message = message ?: status?.reasonPhrase
            this.total = 1
            this.error = null
            this.timestamp = Date()

        }
    }


    /**
     * Return All data in list of Object
     */
    override fun respondList(data: List<Any>?, status: HttpStatus?, message: String?): ResponseDTO {

        return ResponseDTO.apply {
            this.data = data
            this.code = (status?.value() ?: OK.value())
            this.message = message ?: status?.reasonPhrase
            this.total = data?.size?.toLong()
            this.error = null
            this.timestamp = Date()

        }
    }


    /**
     * Return Page of List of Object
     */

    override fun <T: Any> respondPage(data: Page<T>?, status: HttpStatus?, message: String?): ResponseDTO {
        println("Form XML")

        return ResponseDTO.apply {
            this.data = data?.content
            this.total = data?.totalElements
            this.code = (status?.value() ?: OK.value())
            this.message = message ?: status?.reasonPhrase
            this.error = null
            this.timestamp = Date()

        }
    }


    /**
     * for custom response (ex: report, ...)
     */
    override fun respondDynamic(data: Any?, status: HttpStatus?, message: String?, total: Long): ResponseDTO {

        return ResponseDTO.apply {
            this.data = data
            this.code = (status?.value() ?: OK.value())
            this.message = message?:status?.reasonPhrase
            this.total = total
            this.error = null
            this.timestamp = Date()
        }
    }
}