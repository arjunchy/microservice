package com.employee.EmployeeService.service;

import com.employee.EmployeeService.model.read.AddressReadEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressReadService {

    @PersistenceContext(unitName = "addressPU")
    private EntityManager addressEntityManager;

    @Transactional(transactionManager = "addressTransactionManager", readOnly = true)
    public List<AddressReadEntity> findByEmployeeId(Long employeeId) {
        return addressEntityManager.createQuery(
                        "SELECT a FROM AddressReadEntity a WHERE a.employeeId = :employeeId",
                        AddressReadEntity.class)
                .setParameter("employeeId", employeeId)
                .getResultList();
    }
}