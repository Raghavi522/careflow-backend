package com.careflow.repository;

import com.careflow.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    Optional<Consultation> findByAppointmentId(Long appointmentId);
    List<Consultation> findByDoctorIdOrderByConsultationDateTimeDesc(Long doctorId);
    List<Consultation> findByDoctorUserIdOrderByConsultationDateTimeDesc(Long userId);
    List<Consultation> findByPatientIdOrderByConsultationDateTimeDesc(Long patientId);
    long countByDoctorUserId(Long userId);
}
