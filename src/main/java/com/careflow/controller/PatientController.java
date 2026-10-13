package com.careflow.controller;

import com.careflow.dto.*;
import com.careflow.security.UserPrincipal;
import com.careflow.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'RECEPTIONIST')")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/profile")
    public ResponseEntity<PatientDto> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(patientService.getProfile(userPrincipal.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<PatientDto> updateProfile(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                    @RequestBody PatientDto dto) {
        return ResponseEntity.ok(patientService.updateProfile(userPrincipal.getId(), dto));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(patientService.getDashboardData(userPrincipal.getId()));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDto>> getAppointments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(patientService.getAppointments(userPrincipal.getId()));
    }

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentDto> bookAppointment(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                          @Valid @RequestBody AppointmentCreateDto dto) {
        return ResponseEntity.ok(patientService.bookAppointment(userPrincipal.getId(), dto));
    }

    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<AppointmentDto> cancelAppointment(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                            @PathVariable Long id) {
        return ResponseEntity.ok(patientService.cancelAppointment(id, userPrincipal.getId()));
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<List<PrescriptionDto>> getPrescriptions(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(patientService.getPrescriptions(userPrincipal.getId()));
    }

    @GetMapping("/labs")
    public ResponseEntity<List<LabTestDto>> getLabTests(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(patientService.getLabTests(userPrincipal.getId()));
    }

    @GetMapping("/bills")
    public ResponseEntity<List<BillDto>> getBills(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(patientService.getBills(userPrincipal.getId()));
    }

    @GetMapping("/consultations")
    public ResponseEntity<List<ConsultationDto>> getConsultations(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(patientService.getConsultations(userPrincipal.getId()));
    }
}
