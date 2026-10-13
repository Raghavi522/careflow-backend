package com.careflow.controller;

import com.careflow.dto.*;
import com.careflow.entity.Role;
import com.careflow.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final DepartmentService departmentService;
    private final ReceptionistService receptionistService;
    private final BillingService billingService;
    private final AuditLogService auditLogService;

    public AdminController(AdminService adminService,
                           DepartmentService departmentService,
                           ReceptionistService receptionistService,
                           BillingService billingService,
                           AuditLogService auditLogService) {
        this.adminService = adminService;
        this.departmentService = departmentService;
        this.receptionistService = receptionistService;
        this.billingService = billingService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsDto> getStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        String fullName = payload.get("fullName");
        String phone = payload.get("phone");
        Role role = Role.valueOf(payload.get("role"));

        return ResponseEntity.ok(adminService.createUser(email, password, fullName, phone, role));
    }

    @PostMapping("/doctors")
    public ResponseEntity<DoctorDto> createDoctor(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String password = (String) payload.get("password");
        String fullName = (String) payload.get("fullName");
        String phone = (String) payload.get("phone");
        Long departmentId = Long.valueOf(payload.get("departmentId").toString());
        String specialization = (String) payload.get("specialization");
        String qualification = (String) payload.get("qualification");
        BigDecimal fee = payload.get("consultationFee") != null ?
                new BigDecimal(payload.get("consultationFee").toString()) : BigDecimal.valueOf(50.0);
        String roomNumber = (String) payload.get("roomNumber");

        return ResponseEntity.ok(adminService.createDoctor(email, password, fullName, phone, departmentId, specialization, qualification, fee, roomNumber));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String fullName = payload.get("fullName");
        String phone = payload.get("phone");
        return ResponseEntity.ok(adminService.updateUser(id, fullName, phone));
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDto>> getDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @PostMapping("/departments")
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody DepartmentDto dto) {
        return ResponseEntity.ok(departmentService.createDepartment(dto));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDto dto) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDto>> getAppointments() {
        return ResponseEntity.ok(receptionistService.getAllAppointments());
    }

    @GetMapping("/bills")
    public ResponseEntity<List<BillDto>> getBills() {
        return ResponseEntity.ok(billingService.getAllBills());
    }

    @PostMapping("/bills")
    public ResponseEntity<BillDto> createBill(@Valid @RequestBody BillCreateDto dto) {
        return ResponseEntity.ok(billingService.createBill(dto));
    }

    @PutMapping("/bills/{id}/pay")
    public ResponseEntity<BillDto> markBillAsPaid(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String method = body != null ? body.get("paymentMethod") : "CASH";
        return ResponseEntity.ok(billingService.markBillAsPaid(id, method));
    }

    @PutMapping("/bills/{id}/cancel")
    public ResponseEntity<BillDto> cancelBill(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.cancelBill(id));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogDto>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }
}
