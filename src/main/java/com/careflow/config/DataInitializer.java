package com.careflow.config;

import com.careflow.entity.*;
import com.careflow.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabTestRepository labTestRepository;
    private final BillRepository billRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           DoctorProfileRepository doctorProfileRepository,
                           PatientProfileRepository patientProfileRepository,
                           AppointmentRepository appointmentRepository,
                           ConsultationRepository consultationRepository,
                           PrescriptionRepository prescriptionRepository,
                           LabTestRepository labTestRepository,
                           BillRepository billRepository,
                           NotificationRepository notificationRepository,
                           AuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultationRepository = consultationRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.labTestRepository = labTestRepository;
        this.billRepository = billRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("[CareFlow] Database already contains records. Skipping initial seeding.");
            return;
        }

        logger.info("[CareFlow] Starting initial data seeding for CareFlow Hospital Operations...");

        // 1. Departments
        Department cardiology = departmentRepository.save(new Department("Cardiology", "Comprehensive cardiac diagnostics, interventional cardiology, and preventative heart care."));
        Department neurology = departmentRepository.save(new Department("Neurology", "Expert neurological diagnosis, brain & nerve care, and neuro-rehabilitation."));
        Department generalMed = departmentRepository.save(new Department("General Medicine", "Primary healthcare, routine physicals, and preventative internal medicine."));
        Department pediatrics = departmentRepository.save(new Department("Pediatrics", "Compassionate pediatric care, immunizations, and developmental medicine."));
        Department orthopedics = departmentRepository.save(new Department("Orthopedics", "Joint replacement, trauma care, sports medicine, and bone health."));

        // 2. Demo Users
        // Admin
        User admin = userRepository.save(new User(
                "admin@careflow.com",
                passwordEncoder.encode("Admin@123"),
                "Dr. Elizabeth Vance (Chief Admin)",
                "+1-555-0199",
                Role.ADMIN
        ));

        // Receptionist
        User receptionist = userRepository.save(new User(
                "reception@careflow.com",
                passwordEncoder.encode("Reception@123"),
                "Rachel Green",
                "+1-555-0120",
                Role.RECEPTIONIST
        ));

        // Lab Technician
        User labTech = userRepository.save(new User(
                "lab@careflow.com",
                passwordEncoder.encode("Lab@123"),
                "Alex Rivera",
                "+1-555-0145",
                Role.LAB_TECHNICIAN
        ));

        // Doctor 1
        User doctorUser1 = userRepository.save(new User(
                "doctor@careflow.com",
                passwordEncoder.encode("Doctor@123"),
                "Dr. Sarah Jenkins",
                "+1-555-0101",
                Role.DOCTOR
        ));
        DoctorProfile doc1 = doctorProfileRepository.save(new DoctorProfile(
                doctorUser1,
                cardiology,
                "Interventional Cardiology",
                "MD, FACC - Harvard Medical School",
                BigDecimal.valueOf(120.00),
                "Suite 204"
        ));

        // Doctor 2
        User doctorUser2 = userRepository.save(new User(
                "doctor2@careflow.com",
                passwordEncoder.encode("Doctor@123"),
                "Dr. Marcus Vance",
                "+1-555-0102",
                Role.DOCTOR
        ));
        DoctorProfile doc2 = doctorProfileRepository.save(new DoctorProfile(
                doctorUser2,
                neurology,
                "Clinical Neurophysiology",
                "MD, PhD - Johns Hopkins University",
                BigDecimal.valueOf(150.00),
                "Suite 310"
        ));

        // Patient 1
        User patientUser1 = userRepository.save(new User(
                "patient@careflow.com",
                passwordEncoder.encode("Patient@123"),
                "Johnathan Doe",
                "+1-555-0188",
                Role.PATIENT
        ));
        PatientProfile pat1 = patientProfileRepository.save(new PatientProfile(
                patientUser1,
                LocalDate.of(1990, 5, 14),
                "Male",
                "O+",
                "742 Evergreen Terrace, Springfield",
                "+1-555-0190",
                "Mild seasonal asthma, allergic to penicillin."
        ));

        // Patient 2
        User patientUser2 = userRepository.save(new User(
                "patient2@careflow.com",
                passwordEncoder.encode("Patient@123"),
                "Emily Watson",
                "+1-555-0177",
                Role.PATIENT
        ));
        PatientProfile pat2 = patientProfileRepository.save(new PatientProfile(
                patientUser2,
                LocalDate.of(1985, 11, 22),
                "Female",
                "A+",
                "123 Maple Street, Metro City",
                "+1-555-0195",
                "Hypertension diagnosed 2021."
        ));

        // 3. Appointments
        LocalDateTime now = LocalDateTime.now();
        // Completed past appointment
        Appointment apt1 = appointmentRepository.save(new Appointment(
                patientUser1,
                doc1,
                now.minusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                AppointmentStatus.COMPLETED,
                "Chest tightness during cardio workout",
                "Patient reported episodic palpitations."
        ));

        // Today's appointment
        Appointment apt2 = appointmentRepository.save(new Appointment(
                patientUser1,
                doc1,
                now.withHour(14).withMinute(30).withSecond(0).withNano(0),
                AppointmentStatus.SCHEDULED,
                "Routine ECG & blood pressure follow-up",
                "Review laboratory test results."
        ));

        // Upcoming appointment for patient 2
        Appointment apt3 = appointmentRepository.save(new Appointment(
                patientUser2,
                doc2,
                now.plusDays(3).withHour(11).withMinute(0).withSecond(0).withNano(0),
                AppointmentStatus.SCHEDULED,
                "Persistent migraines and visual auras",
                "First neurology consultation."
        ));

        // 4. Consultation for apt1
        Consultation consult1 = consultationRepository.save(new Consultation(
                apt1,
                doc1,
                patientUser1,
                "Mild chest tightness, resting pulse 88 bpm, BP 132/86 mmHg.",
                "Benign premature ventricular contractions with exertion; mild hypertension.",
                "Advised lifestyle modifications, low sodium diet, and prescribed beta-blocker."
        ));

        // 5. Prescription for consult1
        Prescription presc1 = new Prescription(
                consult1,
                doc1,
                patientUser1,
                "Take with meals in the morning. Avoid sudden cessation."
        );
        PrescriptionItem item1 = new PrescriptionItem(presc1, "Metoprolol Tartrate", "25 mg", "Once Daily", "30 Days", "Take with water after breakfast");
        PrescriptionItem item2 = new PrescriptionItem(presc1, "Aspirin (Low Dose)", "81 mg", "Once Daily", "30 Days", "Take after food");
        presc1.addItem(item1);
        presc1.addItem(item2);
        prescriptionRepository.save(presc1);

        // 6. Lab Tests
        // Completed Lab Test
        LabTest lab1 = new LabTest(
                doc1,
                patientUser1,
                "Lipid Profile & Serum Cholesterol",
                "Biochemistry",
                "Blood (Fasting)",
                "Assess cardiovascular risk profile."
        );
        lab1.setStatus(LabTestStatus.COMPLETED);
        LabTest savedLab1 = labTestRepository.save(lab1);

        LabResult res1 = new LabResult(
                savedLab1,
                labTech,
                "Total Cholesterol: 195 mg/dL, HDL: 48 mg/dL, LDL: 118 mg/dL, Triglycerides: 145 mg/dL.",
                "Total: <200 mg/dL, HDL: >40 mg/dL, LDL: <100 mg/dL",
                "Borderline elevated LDL. Recommended dietary fiber increase and routine follow-up."
        );
        savedLab1.setLabResult(res1);
        labTestRepository.save(savedLab1);

        // In-Progress Lab Test
        LabTest lab2 = new LabTest(
                doc1,
                patientUser1,
                "Complete Blood Count (CBC) with Differential",
                "Hematology",
                "Whole Blood (EDTA)",
                "Routine baseline investigation."
        );
        lab2.setStatus(LabTestStatus.IN_PROGRESS);
        labTestRepository.save(lab2);

        // Requested Lab Test for Patient 2
        LabTest lab3 = new LabTest(
                doc2,
                patientUser2,
                "Serum Electrolytes & Magnesium",
                "Biochemistry",
                "Blood Serum",
                "Evaluate electrolyte balance related to migraine episodes."
        );
        lab3.setStatus(LabTestStatus.REQUESTED);
        labTestRepository.save(lab3);

        // 7. Bills
        Bill bill1 = new Bill(patientUser1, "Cardiology Consultation & Resting ECG", BigDecimal.valueOf(150.00), BillStatus.PAID);
        bill1.setPaidAt(now.minusDays(1));
        bill1.setPaymentMethod("Credit Card (Online)");
        billRepository.save(bill1);

        Bill bill2 = new Bill(patientUser1, "Biochemistry Diagnostic Panel (Lipid Profile)", BigDecimal.valueOf(75.00), BillStatus.PENDING);
        billRepository.save(bill2);

        Bill bill3 = new Bill(patientUser2, "Neurology Initial Assessment", BigDecimal.valueOf(150.00), BillStatus.PENDING);
        billRepository.save(bill3);

        // 8. Notifications
        notificationRepository.save(new Notification(
                patientUser1,
                "Lab Result Available",
                "Your test result for 'Lipid Profile & Serum Cholesterol' is now available to review.",
                "/patient/dashboard"
        ));
        notificationRepository.save(new Notification(
                patientUser1,
                "Appointment Reminder",
                "You have an upcoming appointment with Dr. Sarah Jenkins today at 14:30.",
                "/patient/dashboard"
        ));
        notificationRepository.save(new Notification(
                doctorUser1,
                "Consultation Completed",
                "Consultation notes and prescription registered for Johnathan Doe.",
                "/doctor/dashboard"
        ));

        // 9. Audit Logs
        auditLogRepository.save(new AuditLog(admin, "SYSTEM_INIT", "CareFlow system database initialized with core departments and demo profiles.", "127.0.0.1"));
        auditLogRepository.save(new AuditLog(patientUser1, "LOGIN", "Patient logged in to patient portal.", "127.0.0.1"));
        auditLogRepository.save(new AuditLog(doctorUser1, "RECORD_CONSULTATION", "Recorded consultation notes for patient Johnathan Doe.", "127.0.0.1"));
        auditLogRepository.save(new AuditLog(labTech, "ENTER_LAB_RESULT", "Submitted diagnostic findings for Lipid Profile test.", "127.0.0.1"));

        logger.info("[CareFlow] Initial data seeding completed successfully! All demo roles and clinical flows populated.");
    }
}
