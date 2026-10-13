package com.careflow.dto;

import jakarta.validation.constraints.NotBlank;

public class AiRequestDto {

    @NotBlank(message = "Message prompt cannot be empty")
    private String prompt;

    // Optional category/mode: "FAQ", "APPOINTMENT_PREP", "CONSULTATION_SUMMARY", "ADMIN_SUMMARY"
    private String type;

    private String context;

    public AiRequestDto() {
    }

    public AiRequestDto(String prompt, String type, String context) {
        this.prompt = prompt;
        this.type = type;
        this.context = context;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
