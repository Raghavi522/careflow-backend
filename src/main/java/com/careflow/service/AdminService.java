package com.careflow.service;

import com.careflow.dto.*;
import com.careflow.entity.*;
import com.careflow.exception.ConflictException;
import com.careflow.exception.ResourceNotFoundException;
import com.careflow.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final LabTestRepository labTestRepository;
    private final BillRepository billRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public AdminService(UserRepository userRepository,
                        DoctorProfileRepository doctorProfileRepository,
                        DepartmentRepository departmentRepository,
                        AppointmentRepository appointmentRepository,
                        LabTestRepository labTestRepository,
                        BillRepository billRepository,
                        AuditLogRepository auditLogRepository,
                        PasswordEncoder passwordEncoder,
                        AuditLogService auditLogService,
                        DoctorService doctorService,
                        PatientService patientService) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.labTestRepository = labTestRepository;
        this.billRepository = billRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    @Transactional(readOnly = true)
    public StatsDto getSystemStats() {
        long totalPatients = userRepository.countByRole(Role.PATIENT);
        long totalDoctors = userRepository.countByRole(Role.DOCTOR);
        long totalReceptionists = userRepository.countByRole(Role.RECEPTIONIST);
        long totalTechnicians = userRepository.countByRole(Role.LAB_TECHNICIAN);

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        long todayAppointments = appointmentRepository.countByAppointmentDateTimeBetween(start, end);

        long pendingLabTests = labTestRepository.countByStatus(LabTestStatus.REQUESTED) + labTestRepository.countByStatus(LabTestStatus.IN_PROGRESS);
        long pendingBills = billRepository.countByStatus(BillStatus.PENDING);

        List<AuditLogDto> recentActivity = auditLogService.getRecentLogs();

        return new StatsDto(
                totalPatients,
                totalDoctors,
                totalReceptionists,
                totalTechnicians,
                todayAppointments,
                pendingLabTests,
                pendingBills,
                recentActivity
        );
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto createUser(String email, String password, String fullName, String phone, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with email " + email + " already exists");
        }

        User user = new User(
                email,
                passwordEncoder.encode(password),
                fullName,
                phone,
                role
        );
        User saved = userRepository.save(user);

        auditLogService.log(saved, "CREATE_USER", "Admin created " + role.name() + " user: " + email, null);

        return mapUserToDto(saved);
    }

    @Transactional
    public DoctorDto createDoctor(String email, String password, String fullName, String phone,
                                 Long departmentId, String specialization, String qualification,
                                 BigDecimal fee, String roomNumber) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with email " + email + " already exists");
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        User user = new User(
                email,
                passwordEncoder.encode(password),
                fullName,
                phone,
                Role.DOCTOR
        );
        User savedUser = userRepository.save(user);

        DoctorProfile profile = new DoctorProfile(
                savedUser,
                department,
                specialization,
                qualification,
                fee != null ? fee : BigDecimal.valueOf(50.0),
                roomNumber
        );
        DoctorProfile savedProfile = doctorProfileRepository.save(profile);

        auditLogService.log(savedUser, "CREATE_DOCTOR", "Admin created doctor: Dr. " + fullName + " (" + department.getName() + ")", null);

        return doctorService.mapDoctorToDto(savedProfile);
    }

    @Transactional
    public UserDto toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setActive(!user.isActive());
        User saved = userRepository.save(user);

        String action = saved.isActive() ? "ACTIVATE_USER" : "DEACTIVATE_USER";
        auditLogService.log(saved, action, "Admin set user status to " + (saved.isActive() ? "ACTIVE" : "INACTIVE"), null);

        return mapUserToDto(saved);
    }

    @Transactional
    public UserDto updateUser(Long userId, String fullName, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        User saved = userRepository.save(user);

        auditLogService.log(saved, "UPDATE_USER", "Admin updated details for user: " + user.getEmail(), null);

        return mapUserToDto(saved);
    }

    public UserDto mapUserToDto(User u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getPhone(),
                u.getRole(),
                u.isActive(),
                u.getCreatedAt()
        );
    }
}
