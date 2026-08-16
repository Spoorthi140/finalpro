package com.pharmacy.inventory.service.impl;

import com.pharmacy.inventory.dto.request.LoginRequest;
import com.pharmacy.inventory.dto.request.RegisterRequest;
import com.pharmacy.inventory.dto.response.UserResponse;
import com.pharmacy.inventory.entity.User;
import com.pharmacy.inventory.repository.UserRepository;
import com.pharmacy.inventory.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and Confirm Password do not match");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(request.getFullName().trim(), normalizedEmail, hashedPassword, "USER");
        User savedUser = userRepository.save(user);

        return convertToResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return convertToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserResponse(String email) {
        if (email == null) return null;
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElse(null);
        return user != null ? convertToResponse(user) : null;
    }

    private UserResponse convertToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
