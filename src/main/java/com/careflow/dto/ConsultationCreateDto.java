package com.careflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConsultationCreateDto {

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    private String symptoms;

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    private String doctorNotes;

    public ConsultationCreateDto() {
    }

    public ConsultationCreateDto(Long appointmentId, String symptoms, String diagnosis, String doctorNotes) {
        this.appointmentId = appointmentId;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.doctorNotes = doctorNotes;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }
}
