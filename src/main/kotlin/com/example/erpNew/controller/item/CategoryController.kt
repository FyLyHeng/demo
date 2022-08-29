package com.example.erpNew.controller.item

import com.example.erpNew.base.GenericRestfulController
import com.example.erpNew.model.item.Category
import com.example.erpNew.utilities.AppConstant
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/category")
class CategoryController : GenericRestfulController<Category>(resource = Category::class.java, allowUpdate = true, allowDelete = true) {


    override fun afterSaved(entity: Category) {
        println("After save Success we can do any process with the created OBJ")
    }

    override fun listCriteria(allParams: Map<String, String>): Page<Category>? {

        val name = allParams["name"]
        val age = allParams["age"]

        return listCriteria(allParams){ predicates, cb, root ->

            name?.let { predicates.add(cb.equal(root.get<String>("name"), it)) }
            age?.let { predicates.add(cb.equal(root.get<Long>("age"), it.toLong())) }
        }
    }
}