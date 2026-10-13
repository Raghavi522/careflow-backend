package com.careflow.service;

import com.careflow.dto.AuthRequest;
import com.careflow.dto.AuthResponse;
import com.careflow.dto.RegisterRequest;
import com.careflow.entity.PatientProfile;
import com.careflow.entity.Role;
import com.careflow.entity.User;
import com.careflow.exception.ConflictException;
import com.careflow.exception.UnauthorizedException;
import com.careflow.repository.PatientProfileRepository;
import com.careflow.repository.UserRepository;
import com.careflow.security.JwtTokenProvider;
import com.careflow.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PatientProfileRepository patientProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuditLogService auditLogService,
                       NotificationService notificationService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    public AuthResponse login(AuthRequest request, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account has been deactivated. Please contact administrator.");
        }

        String jwt = tokenProvider.generateToken(authentication);

        auditLogService.log(user, "LOGIN", "User logged in successfully", ipAddress);

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getPhone()
        );
    }

    @Transactional
    public AuthResponse registerPatient(RegisterRequest request, String ipAddress) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered: " + request.getEmail());
        }

        // Public patient registration strictly enforces PATIENT role
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
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

        auditLogService.log(savedUser, "REGISTRATION", "New patient account registered: " + savedUser.getEmail(), ipAddress);

        notificationService.createNotification(
                savedUser,
                "Welcome to CareFlow!",
                "Your patient account has been created successfully. You can now book appointments, view prescriptions, and track lab tests.",
                "/patient/dashboard"
        );

        String jwt = tokenProvider.generateTokenFromUser(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getFullName()
        );

        return new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole(),
                savedUser.getPhone()
        );
    }
}
