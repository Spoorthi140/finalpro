package com.pharmacy.inventory.controller;

import com.pharmacy.inventory.dto.request.LoginRequest;
import com.pharmacy.inventory.dto.request.RegisterRequest;
import com.pharmacy.inventory.dto.response.ApiResponse;
import com.pharmacy.inventory.dto.response.UserResponse;
import com.pharmacy.inventory.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final String AUTH_USER_SESSION_KEY = "AUTH_USER_EMAIL";

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        try {
            UserResponse userResponse = authService.register(request);
            HttpSession session = servletRequest.getSession(true);
            session.setAttribute(AUTH_USER_SESSION_KEY, userResponse.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registration successful", userResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        try {
            UserResponse userResponse = authService.login(request);
            HttpSession session = servletRequest.getSession(true);
            session.setAttribute(AUTH_USER_SESSION_KEY, userResponse.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Login successful", userResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession(false);
        if (session == null || session.getAttribute(AUTH_USER_SESSION_KEY) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }

        String email = (String) session.getAttribute(AUTH_USER_SESSION_KEY);
        UserResponse userResponse = authService.getUserResponse(email);

        if (userResponse == null) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not found"));
        }

        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userResponse));
    }
}
