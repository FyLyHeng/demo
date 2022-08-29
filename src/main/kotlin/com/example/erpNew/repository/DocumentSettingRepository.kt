package com.example.erpNew.repository

import com.example.erpNew.base.BaseRepository
import com.example.erpNew.model.setting.DocumentSetting
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DocumentSettingRepository : BaseRepository<DocumentSetting> {
    fun findByIdAndStatusTrue(id: Long): DocumentSetting?
    fun findAllByStatusTrueOrderByIdDesc(): List<DocumentSetting>?
    fun findByName(name: String): DocumentSetting?
    fun findAllByNameIn(listName:List<String>) : List<DocumentSetting>?

    @Modifying
    @Query("update DocumentSetting set lastCode = lastCode+1")
    fun updateLastCode()

    @Modifying
    @Query("update DocumentSetting set nonVatLastCode = nonVatLastCode+1")
    fun updateNonVatLastCode()
}
