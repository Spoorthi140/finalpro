package com.pharmacy.inventory.service;

import com.pharmacy.inventory.dto.request.LoginRequest;
import com.pharmacy.inventory.dto.request.RegisterRequest;
import com.pharmacy.inventory.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    UserResponse login(LoginRequest request);

    UserResponse getUserResponse(String email);
}
