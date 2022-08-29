package com.ig.erp.service.implement.sale

import com.ig.erp.responseFormat.exception.CustomNotAcceptableException
import com.ig.erp.responseFormat.exception.CustomNotFoundException
import com.ig.erp.model.model_sale.DeliveryType
import com.ig.erp.repository.sale.DeliveryTypeRepository
import com.ig.erp.service.services.DeliveryTypeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

import org.springframework.stereotype.Service
import java.util.ArrayList
import javax.persistence.criteria.Predicate

@Service
class DeliveryTypeServiceImpl : DeliveryTypeService {

    @Autowired
    lateinit var deliveryTypeRepository: DeliveryTypeRepository

    override fun findAllList(q: String?, page: Int, size: Int): Page<DeliveryType>? {
        return deliveryTypeRepository.findAll({ root, _, cb ->
            val predicates = ArrayList<Predicate>()
            if (q != null) {
                val type = cb.like(cb.upper(root.get("type")), "%${q.toUpperCase()}%")
                val description = cb.like(cb.upper(root.get("description")), "%${q.toUpperCase()}%")
                predicates.add(cb.or(type, description))
            }
            predicates.add(cb.isTrue(root.get<Boolean>("status")))
            cb.and(*predicates.toTypedArray())
        }, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
    }
    override fun findById(id: Long): DeliveryType? {
        checkDeliveryTypeExceptionById(id)
        return deliveryTypeRepository.findByIdAndStatusTrue(id)
    }

    override fun addNew(t: DeliveryType): DeliveryType? {
        checkDeliveryTypeExceptions(t)
        return  deliveryTypeRepository.save(t)
    }

    override fun updateObj(id: Long, t: DeliveryType): DeliveryType? {
        checkDeliveryTypeExceptionById(id)
        val deliveryType = deliveryTypeRepository.findByIdAndStatusTrue(id)
        if (!t.status) deliveryType?.status = false
        else{
            checkDeliveryTypeExceptions(t)
            deliveryType?.type = t.type
            deliveryType?.description = t.description
        }
        return deliveryTypeRepository.save(deliveryType!!)
    }

    override fun findAll(): List<DeliveryType>? {
        return deliveryTypeRepository.findAllByStatusTrueOrderByIdDesc()
    }

    private fun checkDeliveryTypeExceptionById(id: Long) {
        if (deliveryTypeRepository.findByIdAndStatusTrue(id) == null) throw CustomNotFoundException("Delivery Type id $id doesn't exits")
    }

    private fun checkDeliveryTypeExceptions(deliveryType: DeliveryType) {
        //Check require fields
        if (deliveryType.type == null) throw CustomNotAcceptableException("Invalid Delivery type require type")
    }
}