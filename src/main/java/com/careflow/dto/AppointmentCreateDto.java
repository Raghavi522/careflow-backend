package com.careflow.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AppointmentCreateDto {

    private Long patientId; // optional for patient making appointment (taken from current principal if not supplied), required for receptionist/admin

    @NotNull(message = "Doctor ID is required")
    private Long doctorId; // DoctorProfile id

    @NotNull(message = "Appointment date and time is required")
    private LocalDateTime appointmentDateTime;

    private String reason;
    private String notes;

    public AppointmentCreateDto() {
    }

    public AppointmentCreateDto(Long patientId, Long doctorId, LocalDateTime appointmentDateTime, String reason, String notes) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDateTime = appointmentDateTime;
        this.reason = reason;
        this.notes = notes;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
