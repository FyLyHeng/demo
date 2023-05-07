package com.example.demo.core

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import th.co.geniustree.springdata.jpa.repository.JpaSpecificationExecutorWithProjection

@NoRepositoryBean
interface BaseRepository <T: BaseEntity> : JpaRepository<T,Long>, JpaSpecificationExecutor<T>, JpaSpecificationExecutorWithProjection<T,Long> {
    fun findAllByIdInAndStatus(ids:List<Long>, status:Boolean) : MutableList<T>
    fun findAllByStatusIsTrue() : MutableList<T>
}
