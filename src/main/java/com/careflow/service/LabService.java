package com.careflow.service;

import com.careflow.dto.LabResultCreateDto;
import com.careflow.dto.LabTestDto;
import com.careflow.entity.LabResult;
import com.careflow.entity.LabTest;
import com.careflow.entity.LabTestStatus;
import com.careflow.entity.User;
import com.careflow.exception.BadRequestException;
import com.careflow.exception.ResourceNotFoundException;
import com.careflow.repository.LabResultRepository;
import com.careflow.repository.LabTestRepository;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LabService {

    private final LabTestRepository labTestRepository;
    private final LabResultRepository labResultRepository;
    private final UserRepository userRepository;
    private final PatientService patientService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public LabService(LabTestRepository labTestRepository,
                      LabResultRepository labResultRepository,
                      UserRepository userRepository,
                      PatientService patientService,
                      NotificationService notificationService,
                      AuditLogService auditLogService) {
        this.labTestRepository = labTestRepository;
        this.labResultRepository = labResultRepository;
        this.userRepository = userRepository;
        this.patientService = patientService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<LabTestDto> getAllLabTests() {
        return labTestRepository.findAllByOrderByRequestedAtDesc()
                .stream()
                .map(patientService::mapLabTestToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LabTestDto> getPendingTests() {
        return labTestRepository.findByStatusOrderByRequestedAtDesc(LabTestStatus.REQUESTED)
                .stream()
                .map(patientService::mapLabTestToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LabTestDto> getInProgressTests() {
        return labTestRepository.findByStatusOrderByRequestedAtDesc(LabTestStatus.IN_PROGRESS)
                .stream()
                .map(patientService::mapLabTestToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LabTestDto> getCompletedTests() {
        return labTestRepository.findByStatusOrderByRequestedAtDesc(LabTestStatus.COMPLETED)
                .stream()
                .map(patientService::mapLabTestToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public LabTestDto updateStatus(Long testId, LabTestStatus newStatus) {
        LabTest labTest = labTestRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + testId));

        labTest.setStatus(newStatus);
        LabTest saved = labTestRepository.save(labTest);

        auditLogService.log((User) null, "UPDATE_LAB_STATUS", "Lab test #" + testId + " status updated to " + newStatus, null);

        return patientService.mapLabTestToDto(saved);
    }

    @Transactional
    public LabTestDto enterResult(Long testId, Long technicianId, LabResultCreateDto dto) {
        LabTest labTest = labTestRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + testId));

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician user not found"));

        if (labTest.getStatus() == LabTestStatus.COMPLETED && labTest.getLabResult() != null) {
            // Update existing result
            LabResult result = labTest.getLabResult();
            result.setFindings(dto.getFindings());
            result.setNormalRange(dto.getNormalRange());
            result.setComments(dto.getComments());
            result.setTechnician(technician);
            labResultRepository.save(result);
        } else {
            // Create new result
            LabResult result = new LabResult(
                    labTest,
                    technician,
                    dto.getFindings(),
                    dto.getNormalRange(),
                    dto.getComments()
            );
            labTest.setLabResult(result);
            labTest.setStatus(LabTestStatus.COMPLETED);
            labTestRepository.save(labTest);
        }

        auditLogService.log(technician, "ENTER_LAB_RESULT", "Entered lab results for test #" + testId + " (" + labTest.getTestName() + ")", null);

        // Notify patient
        notificationService.createNotification(
                labTest.getPatient(),
                "Lab Result Available",
                "Your test result for '" + labTest.getTestName() + "' is now ready to view.",
                "/patient/dashboard"
        );

        // Notify doctor
        notificationService.createNotification(
                labTest.getDoctor().getUser(),
                "Lab Result Ready",
                "Test result for patient " + labTest.getPatient().getFullName() + " (" + labTest.getTestName() + ") has been uploaded.",
                "/doctor/dashboard"
        );

        return patientService.mapLabTestToDto(labTest);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardData() {
        List<LabTestDto> pending = getPendingTests();
        List<LabTestDto> inProgress = getInProgressTests();
        List<LabTestDto> completed = getCompletedTests();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("pendingTests", pending);
        dashboard.put("inProgressTests", inProgress);
        dashboard.put("completedTests", completed);
        dashboard.put("totalPendingCount", pending.size());
        dashboard.put("totalInProgressCount", inProgress.size());
        dashboard.put("totalCompletedCount", completed.size());

        return dashboard;
    }
}
