package com.careflow.service;

import com.careflow.dto.*;
import com.careflow.entity.*;
import com.careflow.exception.BadRequestException;
import com.careflow.exception.ConflictException;
import com.careflow.exception.ResourceNotFoundException;
import com.careflow.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReceptionistService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public ReceptionistService(UserRepository userRepository,
                               PatientProfileRepository patientProfileRepository,
                               DoctorProfileRepository doctorProfileRepository,
                               DepartmentRepository departmentRepository,
                               AppointmentRepository appointmentRepository,
                               PasswordEncoder passwordEncoder,
                               NotificationService notificationService,
                               AuditLogService auditLogService,
                               PatientService patientService,
                               DoctorService doctorService) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }

    @Transactional
    public PatientDto registerPatient(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "Patient@123"),
                request.getFullName(),
                request.getPhone(),
                Role.PATIENT
        );
        User savedUser = userRepository.save(user);

        PatientProfile profile = new PatientProfile(
                savedUser,
                request.getDateOfBirth(),
                request.getGender(),
                request.getBloodGroup(),
                request.getAddress(),
                request.getEmergencyContact(),
                null
        );
        patientProfileRepository.save(profile);

        auditLogService.log(savedUser, "RECEPTIONIST_REGISTER_PATIENT", "Receptionist registered patient: " + savedUser.getEmail(), null);

        notificationService.createNotification(
                savedUser,
                "Registered at CareFlow",
                "You have been registered at CareFlow Hospital. You can log in using your email.",
                "/patient/dashboard"
        );

        return patientService.getProfile(savedUser.getId());
    }

    @Transactional(readOnly = true)
    public List<PatientDto> searchPatients(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findByRole(Role.PATIENT)
                    .stream()
                    .map(u -> patientService.getProfile(u.getId()))
                    .collect(Collectors.toList());
        }
        return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
                .stream()
                .filter(u -> u.getRole() == Role.PATIENT)
                .map(u -> patientService.getProfile(u.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> getAllDoctors() {
        return doctorProfileRepository.findAll()
                .stream()
                .map(doctorService::mapDoctorToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentDto scheduleAppointment(AppointmentCreateDto dto) {
        if (dto.getPatientId() == null) {
            throw new BadRequestException("Patient ID is required for receptionist booking");
        }

        User patient = userRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        DoctorProfile doctor = doctorProfileRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + dto.getDoctorId()));

        if (dto.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointment time must be in the future");
        }

        // STRICT CONFLICT CHECK: Doctor cannot have 2 active appointments at same date/time
        if (appointmentRepository.existsConflict(doctor.getId(), dto.getAppointmentDateTime(), AppointmentStatus.CANCELLED)) {
            throw new ConflictException("Scheduling conflict: Dr. " + doctor.getUser().getFullName() + " already has an appointment scheduled at " + dto.getAppointmentDateTime());
        }

        Appointment appointment = new Appointment(
                patient,
                doctor,
                dto.getAppointmentDateTime(),
                AppointmentStatus.SCHEDULED,
                dto.getReason(),
                dto.getNotes()
        );
        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(patient, "RECEPTIONIST_SCHEDULE_APPOINTMENT", "Receptionist scheduled appointment #" + saved.getId() + " with Dr. " + doctor.getUser().getFullName(), null);

        notificationService.createNotification(
                doctor.getUser(),
                "New Appointment Scheduled",
                "Appointment #" + saved.getId() + " scheduled with patient " + patient.getFullName() + " for " + dto.getAppointmentDateTime(),
                "/doctor/dashboard"
        );

        notificationService.createNotification(
                patient,
                "Appointment Scheduled",
                "Your appointment with Dr. " + doctor.getUser().getFullName() + " has been scheduled for " + dto.getAppointmentDateTime(),
                "/patient/dashboard"
        );

        return patientService.mapAppointmentToDto(saved);
    }

    @Transactional
    public AppointmentDto rescheduleAppointment(Long appointmentId, LocalDateTime newDateTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("New appointment time must be in the future");
        }

        // Conflict check excluding current appointment
        if (appointmentRepository.existsConflictExcluding(appointment.getDoctor().getId(), appointmentId, newDateTime, AppointmentStatus.CANCELLED)) {
            throw new ConflictException("Scheduling conflict: Dr. " + appointment.getDoctor().getUser().getFullName() + " is not available at " + newDateTime);
        }

        appointment.setAppointmentDateTime(newDateTime);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(appointment.getPatient(), "RESCHEDULE_APPOINTMENT", "Appointment #" + appointmentId + " rescheduled to " + newDateTime, null);

        notificationService.createNotification(
                appointment.getDoctor().getUser(),
                "Appointment Rescheduled",
                "Appointment #" + appointmentId + " with " + appointment.getPatient().getFullName() + " was rescheduled to " + newDateTime,
                "/doctor/dashboard"
        );

        notificationService.createNotification(
                appointment.getPatient(),
                "Appointment Rescheduled",
                "Your appointment #" + appointmentId + " with Dr. " + appointment.getDoctor().getUser().getFullName() + " was rescheduled to " + newDateTime,
                "/patient/dashboard"
        );

        return patientService.mapAppointmentToDto(saved);
    }

    @Transactional
    public AppointmentDto cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(appointment.getPatient(), "RECEPTIONIST_CANCEL_APPOINTMENT", "Receptionist cancelled appointment #" + appointmentId, null);

        notificationService.createNotification(
                appointment.getDoctor().getUser(),
                "Appointment Cancelled",
                "Appointment #" + appointmentId + " with " + appointment.getPatient().getFullName() + " has been cancelled.",
                "/doctor/dashboard"
        );

        notificationService.createNotification(
                appointment.getPatient(),
                "Appointment Cancelled",
                "Your appointment #" + appointmentId + " has been cancelled by the reception.",
                "/patient/dashboard"
        );

        return patientService.mapAppointmentToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(patientService::mapAppointmentToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getTodayAppointments() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return appointmentRepository.findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(start, end)
                .stream()
                .map(patientService::mapAppointmentToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardData() {
        List<AppointmentDto> today = getTodayAppointments();
        List<PatientDto> allPatients = searchPatients("");
        List<DoctorDto> allDoctors = getAllDoctors();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("todayAppointments", today);
        dashboard.put("totalPatients", allPatients.size());
        dashboard.put("totalDoctors", allDoctors.size());
        dashboard.put("todayAppointmentsCount", today.size());

        return dashboard;
    }
}
