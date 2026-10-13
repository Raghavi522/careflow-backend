package com.careflow.controller;

import com.careflow.dto.LabResultCreateDto;
import com.careflow.dto.LabTestDto;
import com.careflow.entity.LabTestStatus;
import com.careflow.security.UserPrincipal;
import com.careflow.service.LabService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab")
@PreAuthorize("hasAnyRole('LAB_TECHNICIAN', 'ADMIN')")
public class LabController {

    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        return ResponseEntity.ok(labService.getDashboardData());
    }

    @GetMapping("/tests")
    public ResponseEntity<List<LabTestDto>> getAllTests() {
        return ResponseEntity.ok(labService.getAllLabTests());
    }

    @GetMapping("/tests/pending")
    public ResponseEntity<List<LabTestDto>> getPendingTests() {
        return ResponseEntity.ok(labService.getPendingTests());
    }

    @GetMapping("/tests/in-progress")
    public ResponseEntity<List<LabTestDto>> getInProgressTests() {
        return ResponseEntity.ok(labService.getInProgressTests());
    }

    @GetMapping("/tests/completed")
    public ResponseEntity<List<LabTestDto>> getCompletedTests() {
        return ResponseEntity.ok(labService.getCompletedTests());
    }

    @PutMapping("/tests/{id}/status")
    public ResponseEntity<LabTestDto> updateStatus(@PathVariable Long id, @RequestParam("status") LabTestStatus status) {
        return ResponseEntity.ok(labService.updateStatus(id, status));
    }

    @PostMapping("/tests/{id}/result")
    public ResponseEntity<LabTestDto> enterResult(@PathVariable Long id,
                                                 @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                 @Valid @RequestBody LabResultCreateDto dto) {
        return ResponseEntity.ok(labService.enterResult(id, userPrincipal.getId(), dto));
    }
}
