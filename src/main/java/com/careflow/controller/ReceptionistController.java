package com.careflow.controller;

import com.careflow.dto.*;
import com.careflow.service.ReceptionistService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receptionist")
@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    public ReceptionistController(ReceptionistService receptionistService) {
        this.receptionistService = receptionistService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        return ResponseEntity.ok(receptionistService.getDashboardData());
    }

    @PostMapping("/patients")
    public ResponseEntity<PatientDto> registerPatient(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(receptionistService.registerPatient(request));
    }

    @GetMapping("/patients")
    public ResponseEntity<List<PatientDto>> searchPatients(@RequestParam(value = "query", required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(receptionistService.searchPatients(query));
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        return ResponseEntity.ok(receptionistService.getAllDoctors());
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDto>> getAllAppointments() {
        return ResponseEntity.ok(receptionistService.getAllAppointments());
    }

    @GetMapping("/appointments/today")
    public ResponseEntity<List<AppointmentDto>> getTodayAppointments() {
        return ResponseEntity.ok(receptionistService.getTodayAppointments());
    }

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentDto> scheduleAppointment(@Valid @RequestBody AppointmentCreateDto dto) {
        return ResponseEntity.ok(receptionistService.scheduleAppointment(dto));
    }

    @PutMapping("/appointments/{id}/reschedule")
    public ResponseEntity<AppointmentDto> rescheduleAppointment(
            @PathVariable Long id,
            @RequestParam("dateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDateTime) {
        return ResponseEntity.ok(receptionistService.rescheduleAppointment(id, newDateTime));
    }

    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<AppointmentDto> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(receptionistService.cancelAppointment(id));
    }
}
