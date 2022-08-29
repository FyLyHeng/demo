package com.example.erpNew.controller.item

import com.example.erpNew.base.GenericRestfulController
import com.example.erpNew.model.item.Item
import com.example.erpNew.repository.item.CategoryRepository
import com.example.erpNew.responseFormat.exception.entityExecption.NotFoundException
import com.example.erpNew.service.stock.StockTransactionServiceImp
import com.example.erpNew.utilities.AppConstant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(AppConstant.MAIN_PATH+"/item")
class ItemController : GenericRestfulController<Item>(Item::class.java){

    @Autowired
    lateinit var categoryRepo : CategoryRepository
    @Autowired
    lateinit var stockTransactionService: StockTransactionServiceImp

    override fun beforeSave(entity: Item) {
        val cat = categoryRepo.findById(entity.category!!.id!!)
        if (cat.isEmpty){
            println("isEmpty")
            throw NotFoundException("Category not exist.")
        }
    }

    override fun create(entity: Item): Item? {
        entity.name = "liza"
        return super.create(entity)
    }

    override fun afterSaved(entity: Item) {
        stockTransactionService.recordStockTransaction(item = entity, 0, Date(), "Add New Item to Stock")
    }
}