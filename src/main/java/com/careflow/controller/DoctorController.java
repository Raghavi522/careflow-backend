package com.careflow.controller;

import com.careflow.dto.*;
import com.careflow.security.UserPrincipal;
import com.careflow.service.DoctorService;
import com.careflow.service.ReceptionistService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final ReceptionistService receptionistService;

    public DoctorController(DoctorService doctorService, ReceptionistService receptionistService) {
        this.doctorService = doctorService;
        this.receptionistService = receptionistService;
    }

    // Public / all authenticated users can view doctor directory for booking
    @GetMapping("/public")
    public ResponseEntity<List<DoctorDto>> getPublicDoctors() {
        return ResponseEntity.ok(receptionistService.getAllDoctors());
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<DoctorDto> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(doctorService.getProfile(userPrincipal.getId()));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardData(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(doctorService.getDashboardData(userPrincipal.getId()));
    }

    @GetMapping("/appointments")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<AppointmentDto>> getAllAppointments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(doctorService.getAllDoctorAppointments(userPrincipal.getId()));
    }

    @GetMapping("/appointments/today")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<AppointmentDto>> getTodayAppointments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(doctorService.getTodayAppointments(userPrincipal.getId()));
    }

    @GetMapping("/appointments/upcoming")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<AppointmentDto>> getUpcomingAppointments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(doctorService.getUpcomingAppointments(userPrincipal.getId()));
    }

    @PostMapping("/consultations")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ConsultationDto> createConsultation(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                              @Valid @RequestBody ConsultationCreateDto dto) {
        return ResponseEntity.ok(doctorService.createConsultation(userPrincipal.getId(), dto));
    }

    @PostMapping("/prescriptions")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<PrescriptionDto> createPrescription(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                              @Valid @RequestBody PrescriptionCreateDto dto) {
        return ResponseEntity.ok(doctorService.createPrescription(userPrincipal.getId(), dto));
    }

    @PostMapping("/lab-tests")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<LabTestDto> requestLabTest(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                     @Valid @RequestBody LabTestCreateDto dto) {
        return ResponseEntity.ok(doctorService.requestLabTest(userPrincipal.getId(), dto));
    }

    @GetMapping("/lab-tests")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<LabTestDto>> getLabTests(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(doctorService.getDoctorLabTests(userPrincipal.getId()));
    }

    @GetMapping("/prescriptions")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<PrescriptionDto>> getPrescriptions(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(doctorService.getDoctorPrescriptions(userPrincipal.getId()));
    }

    @GetMapping("/consultations")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<ConsultationDto>> getConsultations(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(doctorService.getDoctorConsultations(userPrincipal.getId()));
    }

    @GetMapping("/patients/{patientId}/history")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getPatientHistory(@PathVariable Long patientId) {
        return ResponseEntity.ok(doctorService.getPatientHistoryForDoctor(patientId));
    }
}
