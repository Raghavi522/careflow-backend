package com.careflow.repository;

import com.careflow.entity.LabTest;
import com.careflow.entity.LabTestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {
    List<LabTest> findByPatientIdOrderByRequestedAtDesc(Long patientId);
    List<LabTest> findByDoctorIdOrderByRequestedAtDesc(Long doctorId);
    List<LabTest> findByDoctorUserIdOrderByRequestedAtDesc(Long userId);
    List<LabTest> findByStatusOrderByRequestedAtDesc(LabTestStatus status);
    List<LabTest> findAllByOrderByRequestedAtDesc();
    long countByStatus(LabTestStatus status);
}
