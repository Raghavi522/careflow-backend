package com.careflow.service;

import com.careflow.dto.NotificationDto;
import com.careflow.entity.Notification;
import com.careflow.entity.User;
import com.careflow.exception.ResourceNotFoundException;
import com.careflow.repository.NotificationRepository;
import com.careflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createNotification(User user, String title, String message, String link) {
        Notification notification = new Notification(user, title, message, link);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createNotification(Long userId, String title, String message, String link) {
        userRepository.findById(userId).ifPresent(user -> {
            Notification notification = new Notification(user, title, message, link);
            notificationRepository.save(notification);
        });
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadStatusFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found for current user");
        }

        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : notifications) {
            n.setReadStatus(true);
        }
        notificationRepository.saveAll(notifications);
    }

    public NotificationDto mapToDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.isReadStatus(),
                n.getCreatedAt(),
                n.getLink()
        );
    }
}
