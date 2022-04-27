package com.example.demo.controller.item

import com.example.demo.base.GenericRestfulController
import com.example.demo.model.item.Item
import com.example.demo.repository.item.CategoryRepository
import com.example.demo.responseFormat.exception.CustomNotFoundException
import com.example.demo.service.stock.StockTransactionServiceImp
import com.example.demo.utilities.AppConstant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/item")
class ItemController : GenericRestfulController<Item>(){

    @Autowired
    lateinit var categoryRepo : CategoryRepository
    @Autowired
    lateinit var stockTransactionService: StockTransactionServiceImp

    override fun beforeSave(entity: Item) {
        val cat = categoryRepo.findById(entity.category!!.id!!)
        if (cat.isEmpty){
            println("isEmpty")
            throw CustomNotFoundException("Category not exist.")
        }
    }

    override fun afterSaved(entity: Item) {
        stockTransactionService.recordStockTransaction(item = entity, 0, Date(), "Add New Item to Stock")
    }
}