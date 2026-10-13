package com.careflow.repository;

import com.careflow.entity.Bill;
import com.careflow.entity.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByPatientIdOrderByBillDateDesc(Long patientId);
    List<Bill> findByStatusOrderByBillDateDesc(BillStatus status);
    List<Bill> findAllByOrderByBillDateDesc();
    long countByStatus(BillStatus status);
}
