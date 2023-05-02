package com.example.demo.controller.item

import com.example.demo.core.GenericRestfulController
import com.example.demo.core.Slf4k
import com.example.demo.core.Slf4k.Companion.log
import com.example.demo.core.responseFormat.response.ResponseDTO
import com.example.demo.model.item.Category
import com.example.demo.utilities.AppConstant
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@Slf4k
@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/category")
class CategoryController : GenericRestfulController<Category>(resource = Category::class.java, allowUpdate = true, allowDelete = true) {


    override fun afterSaved(entity: Category) {
        log.info("")
        println("After save Success we can do any process with the created OBJ")
    }


    // should change allParams (map) to specific model.
    override fun list(allParams: Map<String, String>): ResponseDTO {
        val name = allParams["name"]
        val age = allParams["age"]

        val data = listCriteria(allParams){ predicates, cb, root ->

            name?.let { predicates.add(cb.equal(root.get<String>("name"), it)) }
            age?.let { predicates.add(cb.equal(root.get<Long>("age"), it.toLong())) }
        }

        return JSONFormat.respondPage(data)
    }
}