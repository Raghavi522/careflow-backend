package com.careflow.service;

import com.careflow.dto.*;
import com.careflow.entity.*;
import com.careflow.exception.BadRequestException;
import com.careflow.exception.ResourceNotFoundException;
import com.careflow.repository.*;
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
public class DoctorService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabTestRepository labTestRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final PatientService patientService;

    public DoctorService(UserRepository userRepository,
                         DoctorProfileRepository doctorProfileRepository,
                         PatientProfileRepository patientProfileRepository,
                         AppointmentRepository appointmentRepository,
                         ConsultationRepository consultationRepository,
                         PrescriptionRepository prescriptionRepository,
                         LabTestRepository labTestRepository,
                         NotificationService notificationService,
                         AuditLogService auditLogService,
                         PatientService patientService) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultationRepository = consultationRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.labTestRepository = labTestRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.patientService = patientService;
    }

    @Transactional(readOnly = true)
    public DoctorProfile getDoctorProfileByUserId(Long userId) {
        return doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user id: " + userId));
    }

    @Transactional(readOnly = true)
    public DoctorDto getProfile(Long userId) {
        DoctorProfile profile = getDoctorProfileByUserId(userId);
        return mapDoctorToDto(profile);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getTodayAppointments(Long userId) {
        DoctorProfile profile = getDoctorProfileByUserId(userId);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        return appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(profile.getId(), startOfDay, endOfDay)
                .stream()
                .map(patientService::mapAppointmentToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getUpcomingAppointments(Long userId) {
        DoctorProfile profile = getDoctorProfileByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(30);

        return appointmentRepository.findByDoctorIdAndAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(profile.getId(), now, future)
                .stream()
                .map(patientService::mapAppointmentToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAllDoctorAppointments(Long userId) {
        DoctorProfile profile = getDoctorProfileByUserId(userId);
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeDesc(profile.getId())
                .stream()
                .map(patientService::mapAppointmentToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConsultationDto createConsultation(Long userId, ConsultationCreateDto dto) {
        DoctorProfile doctor = getDoctorProfileByUserId(userId);

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + dto.getAppointmentId()));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("This appointment does not belong to you");
        }

        // Check if consultation already exists
        consultationRepository.findByAppointmentId(appointment.getId()).ifPresent(c -> {
            throw new BadRequestException("Consultation already recorded for this appointment");
        });

        User patient = appointment.getPatient();

        Consultation consultation = new Consultation(
                appointment,
                doctor,
                patient,
                dto.getSymptoms(),
                dto.getDiagnosis(),
                dto.getDoctorNotes()
        );
        Consultation saved = consultationRepository.save(consultation);

        // Mark appointment as COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        auditLogService.log(doctor.getUser(), "RECORD_CONSULTATION", "Recorded consultation for patient: " + patient.getFullName(), null);

        notificationService.createNotification(
                patient,
                "Consultation Completed",
                "Dr. " + doctor.getUser().getFullName() + " has completed your consultation and recorded the diagnosis.",
                "/patient/dashboard"
        );

        return patientService.mapConsultationToDto(saved);
    }

    @Transactional
    public PrescriptionDto createPrescription(Long userId, PrescriptionCreateDto dto) {
        DoctorProfile doctor = getDoctorProfileByUserId(userId);

        User patient = userRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        Consultation consultation = null;
        if (dto.getConsultationId() != null) {
            consultation = consultationRepository.findById(dto.getConsultationId()).orElse(null);
        }

        Prescription prescription = new Prescription(
                consultation,
                doctor,
                patient,
                dto.getGeneralInstructions()
        );

        for (PrescriptionItemDto itemDto : dto.getItems()) {
            PrescriptionItem item = new PrescriptionItem(
                    prescription,
                    itemDto.getMedicineName(),
                    itemDto.getDosage(),
                    itemDto.getFrequency(),
                    itemDto.getDuration(),
                    itemDto.getInstructions()
            );
            prescription.addItem(item);
        }

        Prescription saved = prescriptionRepository.save(prescription);

        auditLogService.log(doctor.getUser(), "CREATE_PRESCRIPTION", "Prescription created for patient: " + patient.getFullName() + " with " + saved.getItems().size() + " medicines", null);

        notificationService.createNotification(
                patient,
                "New Prescription Added",
                "Dr. " + doctor.getUser().getFullName() + " has prescribed medication for you. View prescription details in your dashboard.",
                "/patient/dashboard"
        );

        return patientService.mapPrescriptionToDto(saved);
    }

    @Transactional
    public LabTestDto requestLabTest(Long userId, LabTestCreateDto dto) {
        DoctorProfile doctor = getDoctorProfileByUserId(userId);

        User patient = userRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        LabTest labTest = new LabTest(
                doctor,
                patient,
                dto.getTestName(),
                dto.getTestCategory(),
                dto.getSampleType(),
                dto.getNotes()
        );

        LabTest saved = labTestRepository.save(labTest);

        auditLogService.log(doctor.getUser(), "REQUEST_LAB_TEST", "Requested lab test '" + saved.getTestName() + "' for patient: " + patient.getFullName(), null);

        // Notify patient
        notificationService.createNotification(
                patient,
                "Laboratory Test Requested",
                "Dr. " + doctor.getUser().getFullName() + " has requested a laboratory test: " + saved.getTestName(),
                "/patient/dashboard"
        );

        return patientService.mapLabTestToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<LabTestDto> getDoctorLabTests(Long userId) {
        DoctorProfile doctor = getDoctorProfileByUserId(userId);
        return labTestRepository.findByDoctorIdOrderByRequestedAtDesc(doctor.getId())
                .stream()
                .map(patientService::mapLabTestToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getDoctorPrescriptions(Long userId) {
        DoctorProfile doctor = getDoctorProfileByUserId(userId);
        return prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(doctor.getId())
                .stream()
                .map(patientService::mapPrescriptionToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationDto> getDoctorConsultations(Long userId) {
        DoctorProfile doctor = getDoctorProfileByUserId(userId);
        return consultationRepository.findByDoctorIdOrderByConsultationDateTimeDesc(doctor.getId())
                .stream()
                .map(patientService::mapConsultationToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPatientHistoryForDoctor(Long patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        PatientProfile profile = patientProfileRepository.findByUserId(patientId).orElse(null);

        Map<String, Object> history = new HashMap<>();
        history.put("patient", patientService.getProfile(patientId));
        history.put("consultations", patientService.getConsultations(patientId));
        history.put("prescriptions", patientService.getPrescriptions(patientId));
        history.put("labTests", patientService.getLabTests(patientId));

        return history;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardData(Long userId) {
        DoctorProfile doctor = getDoctorProfileByUserId(userId);
        List<AppointmentDto> todayAppointments = getTodayAppointments(userId);
        List<AppointmentDto> upcomingAppointments = getUpcomingAppointments(userId);
        List<ConsultationDto> consultations = getDoctorConsultations(userId);
        List<LabTestDto> labTests = getDoctorLabTests(userId);

        List<LabTestDto> pendingLabResults = labTests.stream()
                .filter(t -> t.getStatus() == LabTestStatus.REQUESTED || t.getStatus() == LabTestStatus.IN_PROGRESS)
                .collect(Collectors.toList());

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("todayAppointments", todayAppointments);
        dashboard.put("upcomingAppointments", upcomingAppointments);
        dashboard.put("completedConsultationsCount", consultations.size());
        dashboard.put("pendingLabResults", pendingLabResults);
        dashboard.put("totalLabTestsRequested", labTests.size());

        return dashboard;
    }

    public DoctorDto mapDoctorToDto(DoctorProfile d) {
        return new DoctorDto(
                d.getId(),
                d.getUser().getId(),
                d.getUser().getFullName(),
                d.getUser().getEmail(),
                d.getUser().getPhone(),
                d.getDepartment().getId(),
                d.getDepartment().getName(),
                d.getSpecialization(),
                d.getQualification(),
                d.getConsultationFee(),
                d.getRoomNumber()
        );
    }
}
