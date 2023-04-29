package com.example.demo.controller.item

import com.example.demo.core.GenericRestfulController
import com.example.demo.core.responseFormat.response.ResponseDTO
import com.example.demo.model.item.Category
import com.example.demo.utilities.AppConstant
import lombok.extern.slf4j.Slf4j
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/category")
class CategoryController : GenericRestfulController<Category>(resource = Category::class.java, allowUpdate = true, allowDelete = true) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun afterSaved(entity: Category) {
        logger.info("")
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