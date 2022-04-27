package com.example.demo.repository.stock

import com.example.demo.model.stock.StockTransaction
import com.example.demo.base.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface StockRepository : BaseRepository<StockTransaction>