package com.careflow.dto;

import jakarta.validation.constraints.NotBlank;

public class LabResultCreateDto {

    @NotBlank(message = "Findings are required")
    private String findings;

    private String normalRange;
    private String comments;

    public LabResultCreateDto() {
    }

    public LabResultCreateDto(String findings, String normalRange, String comments) {
        this.findings = findings;
        this.normalRange = normalRange;
        this.comments = comments;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
