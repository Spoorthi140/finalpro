package com.smarturban.backend.service;

import com.smarturban.backend.dto.LoginRequest;
import com.smarturban.backend.dto.RegisterRequest;
import com.smarturban.backend.entity.User;
import com.smarturban.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerCitizen(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered: " + request.getEmail());
        }

        User user = new User(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                request.getAddress(),
                "ROLE_CITIZEN"
        );

        return userRepository.save(user);
    }

    public User createUserByAdmin(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email is already registered: " + user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateProfile(Long userId, String fullName, String phone, String address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        user.setFullName(fullName);
        user.setPhone(phone);
        user.setAddress(address);

        return userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        userRepository.delete(user);
    }
}
