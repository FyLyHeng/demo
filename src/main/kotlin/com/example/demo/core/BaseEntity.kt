package com.example.demo.core

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*
import javax.annotation.PreDestroy
import javax.persistence.*


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false, columnDefinition = "BIGINT")
    private val id: Long = 0

    @Column(name = "status", nullable = false,columnDefinition = "boolean default true")
    var status : Boolean = true

    @JsonIgnore(false)
    @Column(name = "version")
    @Version
    var version: Int ?= 0

    @CreatedDate
    @JsonIgnore
    @Column(name = "date_created")
    var created: Date? = null

    @LastModifiedDate
    @JsonIgnore
    @Column(name = "last_updated")
    var updated: Date? = null


    @CreatedBy
    @JsonIgnore
    @Column(name = "created_by_id")
    var createById: Long? = null


    @LastModifiedBy
    @JsonIgnore
    @Column(name = "updated_by_id")
    var updatedById: Long? = null

    @PreDestroy
    protected fun onDelete() {
        status = false
    }

    companion object {

    }
}
