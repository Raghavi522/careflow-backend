package com.careflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PrescriptionCreateDto {

    private Long consultationId;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private String generalInstructions;

    @NotEmpty(message = "At least one prescription item is required")
    @Valid
    private List<PrescriptionItemDto> items;

    public PrescriptionCreateDto() {
    }

    public PrescriptionCreateDto(Long consultationId, Long patientId, String generalInstructions, List<PrescriptionItemDto> items) {
        this.consultationId = consultationId;
        this.patientId = patientId;
        this.generalInstructions = generalInstructions;
        this.items = items;
    }

    public Long getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(Long consultationId) {
        this.consultationId = consultationId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getGeneralInstructions() {
        return generalInstructions;
    }

    public void setGeneralInstructions(String generalInstructions) {
        this.generalInstructions = generalInstructions;
    }

    public List<PrescriptionItemDto> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItemDto> items) {
        this.items = items;
    }
}
