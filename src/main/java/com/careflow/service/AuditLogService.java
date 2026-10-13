package com.careflow.service;

import com.careflow.dto.AuditLogDto;
import com.careflow.entity.AuditLog;
import com.careflow.entity.User;
import com.careflow.repository.AuditLogRepository;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void log(User user, String action, String description, String ipAddress) {
        AuditLog log = new AuditLog(user, action, description, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logById(Long userId, String action, String description, String ipAddress) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        AuditLog log = new AuditLog(user, action, description, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getRecentLogs() {
        return auditLogRepository.findTop20ByOrderByTimestampDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AuditLogDto mapToDto(AuditLog log) {
        return new AuditLogDto(
                log.getId(),
                log.getUser() != null ? log.getUser().getId() : null,
                log.getUser() != null ? log.getUser().getEmail() : "SYSTEM",
                log.getUser() != null ? log.getUser().getRole().name() : "SYSTEM",
                log.getAction(),
                log.getDescription(),
                log.getIpAddress(),
                log.getTimestamp()
        );
    }
}
