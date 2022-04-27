package com.example.demo.controller.item

import com.example.demo.base.GenericRestfulController
import com.example.demo.model.item.Category
import com.example.demo.utilities.AppConstant
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/category")
class CategoryController : GenericRestfulController<Category>(){


    override fun afterSaved(entity: Category) {
        println("After save Success we can do any process with the created OBJ")
    }
}