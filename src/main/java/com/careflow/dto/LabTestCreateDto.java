package com.careflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LabTestCreateDto {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotBlank(message = "Test name is required")
    private String testName;

    private String testCategory;
    private String sampleType;
    private String notes;

    public LabTestCreateDto() {
    }

    public LabTestCreateDto(Long patientId, String testName, String testCategory, String sampleType, String notes) {
        this.patientId = patientId;
        this.testName = testName;
        this.testCategory = testCategory;
        this.sampleType = sampleType;
        this.notes = notes;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestCategory() {
        return testCategory;
    }

    public void setTestCategory(String testCategory) {
        this.testCategory = testCategory;
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
