package com.careflow.repository;

import com.careflow.entity.Appointment;
import com.careflow.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByAppointmentDateTimeDesc(Long patientId);
    List<Appointment> findByDoctorIdOrderByAppointmentDateTimeDesc(Long doctorId);
    List<Appointment> findByDoctorUserIdOrderByAppointmentDateTimeDesc(Long userId);
    List<Appointment> findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(LocalDateTime start, LocalDateTime end);
    List<Appointment> findByDoctorIdAndAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(Long doctorId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByDoctorUserIdAndAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId AND a.status != :cancelledStatus AND a.appointmentDateTime = :dateTime")
    boolean existsConflict(@Param("doctorId") Long doctorId, @Param("dateTime") LocalDateTime dateTime, @Param("cancelledStatus") AppointmentStatus cancelledStatus);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId AND a.id != :excludeId AND a.status != :cancelledStatus AND a.appointmentDateTime = :dateTime")
    boolean existsConflictExcluding(@Param("doctorId") Long doctorId, @Param("excludeId") Long excludeId, @Param("dateTime") LocalDateTime dateTime, @Param("cancelledStatus") AppointmentStatus cancelledStatus);

    long countByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
