package com.careflow.service;

import com.careflow.dto.BillCreateDto;
import com.careflow.dto.BillDto;
import com.careflow.entity.Bill;
import com.careflow.entity.BillStatus;
import com.careflow.entity.User;
import com.careflow.exception.ResourceNotFoundException;
import com.careflow.repository.BillRepository;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final PatientService patientService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public BillingService(BillRepository billRepository,
                          UserRepository userRepository,
                          PatientService patientService,
                          NotificationService notificationService,
                          AuditLogService auditLogService) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.patientService = patientService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<BillDto> getAllBills() {
        return billRepository.findAllByOrderByBillDateDesc()
                .stream()
                .map(patientService::mapBillToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BillDto createBill(BillCreateDto dto) {
        User patient = userRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));

        Bill bill = new Bill(
                patient,
                dto.getDescription(),
                dto.getAmount(),
                BillStatus.PENDING
        );
        Bill saved = billRepository.save(bill);

        auditLogService.log(patient, "CREATE_BILL", "Generated invoice #" + saved.getId() + " for $" + saved.getAmount(), null);

        notificationService.createNotification(
                patient,
                "New Medical Bill Generated",
                "An invoice of $" + saved.getAmount() + " for '" + saved.getDescription() + "' has been generated.",
                "/patient/dashboard"
        );

        return patientService.mapBillToDto(saved);
    }

    @Transactional
    public BillDto markBillAsPaid(Long billId, String paymentMethod) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));

        bill.setStatus(BillStatus.PAID);
        bill.setPaidAt(LocalDateTime.now());
        bill.setPaymentMethod(paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod : "Credit Card / Online");
        Bill saved = billRepository.save(bill);

        auditLogService.log(bill.getPatient(), "PAY_BILL", "Bill #" + billId + " marked as PAID (" + saved.getPaymentMethod() + ")", null);

        notificationService.createNotification(
                bill.getPatient(),
                "Payment Received",
                "Your payment of $" + saved.getAmount() + " for Bill #" + billId + " has been recorded successfully. Thank you!",
                "/patient/dashboard"
        );

        return patientService.mapBillToDto(saved);
    }

    @Transactional
    public BillDto cancelBill(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));

        bill.setStatus(BillStatus.CANCELLED);
        Bill saved = billRepository.save(bill);

        auditLogService.log(bill.getPatient(), "CANCEL_BILL", "Bill #" + billId + " cancelled", null);

        return patientService.mapBillToDto(saved);
    }
}
