package com.example.demo.repository.item

import com.example.demo.model.item.Item
import com.example.demo.core.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : BaseRepository<Item> {
}