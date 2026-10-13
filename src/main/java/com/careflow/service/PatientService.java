package com.careflow.service;

import com.careflow.dto.*;
import com.careflow.entity.*;
import com.careflow.exception.BadRequestException;
import com.careflow.exception.ConflictException;
import com.careflow.exception.ResourceNotFoundException;
import com.careflow.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabTestRepository labTestRepository;
    private final BillRepository billRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public PatientService(UserRepository userRepository,
                          PatientProfileRepository patientProfileRepository,
                          DoctorProfileRepository doctorProfileRepository,
                          AppointmentRepository appointmentRepository,
                          ConsultationRepository consultationRepository,
                          PrescriptionRepository prescriptionRepository,
                          LabTestRepository labTestRepository,
                          BillRepository billRepository,
                          NotificationService notificationService,
                          AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultationRepository = consultationRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.labTestRepository = labTestRepository;
        this.billRepository = billRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PatientDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        PatientProfile profile = patientProfileRepository.findByUserId(userId)
                .orElse(null);

        return new PatientDto(
                profile != null ? profile.getId() : null,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                profile != null ? profile.getDateOfBirth() : null,
                profile != null ? profile.getGender() : null,
                profile != null ? profile.getBloodGroup() : null,
                profile != null ? profile.getAddress() : null,
                profile != null ? profile.getEmergencyContact() : null,
                profile != null ? profile.getMedicalHistoryNotes() : null
        );
    }

    @Transactional
    public PatientDto updateProfile(Long userId, PatientDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        userRepository.save(user);

        PatientProfile profile = patientProfileRepository.findByUserId(userId)
                .orElseGet(() -> new PatientProfile(user, null, null, null, null, null, null));

        if (dto.getDateOfBirth() != null) profile.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) profile.setGender(dto.getGender());
        if (dto.getBloodGroup() != null) profile.setBloodGroup(dto.getBloodGroup());
        if (dto.getAddress() != null) profile.setAddress(dto.getAddress());
        if (dto.getEmergencyContact() != null) profile.setEmergencyContact(dto.getEmergencyContact());
        if (dto.getMedicalHistoryNotes() != null) profile.setMedicalHistoryNotes(dto.getMedicalHistoryNotes());

        patientProfileRepository.save(profile);

        auditLogService.log(user, "UPDATE_PROFILE", "Patient updated profile information", null);

        return getProfile(userId);
    }

    @Transactional
    public AppointmentDto bookAppointment(Long userId, AppointmentCreateDto dto) {
        User patient = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient user not found"));

        DoctorProfile doctor = doctorProfileRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + dto.getDoctorId()));

        if (dto.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointment time must be in the future");
        }

        // Backend conflict check: doctor cannot have 2 active appointments at identical time
        if (appointmentRepository.existsConflict(doctor.getId(), dto.getAppointmentDateTime(), AppointmentStatus.CANCELLED)) {
            throw new ConflictException("Doctor " + doctor.getUser().getFullName() + " already has an appointment scheduled at " + dto.getAppointmentDateTime() + ". Please choose another time slot.");
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

        auditLogService.log(patient, "BOOK_APPOINTMENT", "Booked appointment #" + saved.getId() + " with Dr. " + doctor.getUser().getFullName(), null);

        // Notify Doctor
        notificationService.createNotification(
                doctor.getUser(),
                "New Appointment Booked",
                "Patient " + patient.getFullName() + " booked an appointment for " + dto.getAppointmentDateTime(),
                "/doctor/dashboard"
        );

        // Notify Patient
        notificationService.createNotification(
                patient,
                "Appointment Confirmed",
                "Your appointment with Dr. " + doctor.getUser().getFullName() + " is confirmed for " + dto.getAppointmentDateTime(),
                "/patient/dashboard"
        );

        return mapAppointmentToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointments(Long userId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateTimeDesc(userId)
                .stream()
                .map(this::mapAppointmentToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentDto cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        if (!appointment.getPatient().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to cancel this appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel an already completed appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(appointment.getPatient(), "CANCEL_APPOINTMENT", "Cancelled appointment #" + appointmentId, null);

        // Notify Doctor
        notificationService.createNotification(
                appointment.getDoctor().getUser(),
                "Appointment Cancelled",
                "Patient " + appointment.getPatient().getFullName() + " cancelled the appointment scheduled for " + appointment.getAppointmentDateTime(),
                "/doctor/dashboard"
        );

        return mapAppointmentToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPrescriptions(Long userId) {
        return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapPrescriptionToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LabTestDto> getLabTests(Long userId) {
        return labTestRepository.findByPatientIdOrderByRequestedAtDesc(userId)
                .stream()
                .map(this::mapLabTestToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillDto> getBills(Long userId) {
        return billRepository.findByPatientIdOrderByBillDateDesc(userId)
                .stream()
                .map(this::mapBillToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationDto> getConsultations(Long userId) {
        return consultationRepository.findByPatientIdOrderByConsultationDateTimeDesc(userId)
                .stream()
                .map(this::mapConsultationToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardData(Long userId) {
        List<AppointmentDto> allAppointments = getAppointments(userId);
        List<AppointmentDto> upcomingAppointments = allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.CONFIRMED)
                .collect(Collectors.toList());
        List<AppointmentDto> completedAppointments = allAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .collect(Collectors.toList());

        List<PrescriptionDto> prescriptions = getPrescriptions(userId);
        List<LabTestDto> labTests = getLabTests(userId);
        List<BillDto> bills = getBills(userId);
        List<BillDto> pendingBills = bills.stream()
                .filter(b -> b.getStatus() == BillStatus.PENDING)
                .collect(Collectors.toList());

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("upcomingAppointments", upcomingAppointments);
        dashboard.put("completedAppointments", completedAppointments);
        dashboard.put("recentPrescriptions", prescriptions.stream().limit(5).collect(Collectors.toList()));
        dashboard.put("recentLabResults", labTests.stream().limit(5).collect(Collectors.toList()));
        dashboard.put("pendingBills", pendingBills);
        dashboard.put("totalPendingAmount", pendingBills.stream().map(BillDto::getAmount).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));

        return dashboard;
    }

    public AppointmentDto mapAppointmentToDto(Appointment a) {
        return new AppointmentDto(
                a.getId(),
                a.getPatient().getId(),
                a.getPatient().getFullName(),
                a.getPatient().getEmail(),
                a.getPatient().getPhone(),
                a.getDoctor().getId(),
                a.getDoctor().getUser().getId(),
                a.getDoctor().getUser().getFullName(),
                a.getDoctor().getDepartment().getName(),
                a.getDoctor().getSpecialization(),
                a.getDoctor().getRoomNumber(),
                a.getAppointmentDateTime(),
                a.getStatus(),
                a.getReason(),
                a.getNotes(),
                a.getCreatedAt()
        );
    }

    public PrescriptionDto mapPrescriptionToDto(Prescription p) {
        List<PrescriptionItemDto> items = p.getItems().stream()
                .map(item -> new PrescriptionItemDto(
                        item.getId(),
                        item.getMedicineName(),
                        item.getDosage(),
                        item.getFrequency(),
                        item.getDuration(),
                        item.getInstructions()
                )).collect(Collectors.toList());

        return new PrescriptionDto(
                p.getId(),
                p.getConsultation() != null ? p.getConsultation().getId() : null,
                p.getDoctor().getId(),
                p.getDoctor().getUser().getFullName(),
                p.getPatient().getId(),
                p.getPatient().getFullName(),
                p.getCreatedAt(),
                p.getGeneralInstructions(),
                items
        );
    }

    public LabTestDto mapLabTestToDto(LabTest l) {
        LabTestDto dto = new LabTestDto(
                l.getId(),
                l.getDoctor().getId(),
                l.getDoctor().getUser().getFullName(),
                l.getPatient().getId(),
                l.getPatient().getFullName(),
                l.getTestName(),
                l.getTestCategory(),
                l.getSampleType(),
                l.getStatus(),
                l.getRequestedAt(),
                l.getNotes()
        );
        if (l.getLabResult() != null) {
            LabResult res = l.getLabResult();
            dto.setResultId(res.getId());
            dto.setTechnicianName(res.getTechnician() != null ? res.getTechnician().getFullName() : "Laboratory Staff");
            dto.setCompletedAt(res.getCompletedAt());
            dto.setFindings(res.getFindings());
            dto.setNormalRange(res.getNormalRange());
            dto.setComments(res.getComments());
        }
        return dto;
    }

    public BillDto mapBillToDto(Bill b) {
        return new BillDto(
                b.getId(),
                b.getPatient().getId(),
                b.getPatient().getFullName(),
                b.getPatient().getEmail(),
                b.getDescription(),
                b.getAmount(),
                b.getBillDate(),
                b.getStatus(),
                b.getPaidAt(),
                b.getPaymentMethod()
        );
    }

    public ConsultationDto mapConsultationToDto(Consultation c) {
        return new ConsultationDto(
                c.getId(),
                c.getAppointment() != null ? c.getAppointment().getId() : null,
                c.getDoctor().getId(),
                c.getDoctor().getUser().getFullName(),
                c.getPatient().getId(),
                c.getPatient().getFullName(),
                c.getConsultationDateTime(),
                c.getSymptoms(),
                c.getDiagnosis(),
                c.getDoctorNotes()
        );
    }
}
