package com.careflow.controller;

import com.careflow.dto.AiRequestDto;
import com.careflow.dto.AiResponseDto;
import com.careflow.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final GeminiService geminiService;

    public AiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiResponseDto> chat(@Valid @RequestBody AiRequestDto request) {
        if (request.getType() == null || request.getType().isBlank()) {
            request.setType("FAQ");
        }
        return ResponseEntity.ok(geminiService.processAiRequest(request));
    }

    @PostMapping("/summarize")
    public ResponseEntity<AiResponseDto> summarize(@Valid @RequestBody AiRequestDto request) {
        if (request.getType() == null || request.getType().isBlank()) {
            request.setType("CONSULTATION_SUMMARY");
        }
        return ResponseEntity.ok(geminiService.processAiRequest(request));
    }

    @PostMapping("/appointment-prep")
    public ResponseEntity<AiResponseDto> appointmentPrep(@Valid @RequestBody AiRequestDto request) {
        request.setType("APPOINTMENT_PREP");
        return ResponseEntity.ok(geminiService.processAiRequest(request));
    }

    @PostMapping("/admin-summary")
    public ResponseEntity<AiResponseDto> adminSummary(@Valid @RequestBody AiRequestDto request) {
        request.setType("ADMIN_SUMMARY");
        return ResponseEntity.ok(geminiService.processAiRequest(request));
    }
}
