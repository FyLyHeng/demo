package com.example.demo.controller.item

import com.example.demo.core.GenericRestfulController
import com.example.demo.core.responseFormat.response.ResponseDTO
import com.example.demo.model.item.Item
import com.example.demo.repository.item.CategoryRepository
import com.example.demo.service.stock.StockTransactionServiceImp
import com.example.demo.utilities.AppConstant
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/item")
class ItemController(
    val categoryRepo : CategoryRepository,
    val stockTransactionService: StockTransactionServiceImp
    ) : GenericRestfulController<Item>(){



    override fun create(entity: Item): ResponseDTO {
        val data = super.create(entity) {
            it.name  = "liza"
        }

        //return JSONFormat.respondObj(data)
        return JSONFormat.respondID(data)

    }

    override fun afterSaved(entity: Item) {
        stockTransactionService.recordStockTransaction(item = entity, 0, Date(), "Add New Item to Stock")
    }
}