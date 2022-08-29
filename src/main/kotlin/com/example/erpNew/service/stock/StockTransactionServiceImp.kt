package com.example.erpNew.service.stock

import com.example.erpNew.model.item.Item
import com.example.erpNew.model.stock.StockTransaction
import com.example.erpNew.repository.stock.StockRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Date

@Service
class StockTransactionServiceImp {

    @Autowired
    lateinit var stockRepository: StockRepository


    /**
     * @see: Use this fun for record item transactions:
     *
     * @USING in process:
     *      1 add new Item
     *      2 add Invoice (item sale out)
     *
     * @return void
     */
    fun recordStockTransaction (item: Item,qty:Int, eventDate: Date?=Date(),ref:String?="") {
        stockRepository.save(StockTransaction(item = item, qty = qty, date = eventDate, referenceNo = ref))
    }


}