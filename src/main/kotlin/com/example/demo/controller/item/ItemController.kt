package com.example.demo.controller.item

import com.example.demo.core.GenericRestfulController
import com.example.demo.model.item.Item
import com.example.demo.repository.item.CategoryRepository
import com.example.demo.core.responseFormat.exception.entityExecption.NotFoundException
import com.example.demo.service.stock.StockTransactionServiceImp
import com.example.demo.utilities.AppConstant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/item")
class ItemController(
    val categoryRepo : CategoryRepository,
    val stockTransactionService: StockTransactionServiceImp

    ) : GenericRestfulController<Item>(Item::class.java){


//    @Autowired
//    private lateinit var categoryRepo : CategoryRepository
//    @Autowired
//    private lateinit val stockTransactionService: StockTransactionServiceImp



    override fun create(entity: Item): Item? {
        entity.name = "liza"
        return super.create(entity)
    }

    override fun afterSaved(entity: Item) {
        stockTransactionService.recordStockTransaction(item = entity, 0, Date(), "Add New Item to Stock")
    }
}