package com.careflow.dto;

public class AiResponseDto {

    private String response;
    private String disclaimer = "AI-generated information is for assistance only and should not replace professional medical judgment.";

    public AiResponseDto() {
    }

    public AiResponseDto(String response) {
        this.response = response;
        this.disclaimer = "AI-generated information is for assistance only and should not replace professional medical judgment.";
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
