package com.auth.service;

import com.auth.dto.LoginRequest;
import com.auth.dto.RegisterRequest;
import com.auth.dto.AuthResponse;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import com.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthResponse login(LoginRequest request) throws Exception {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        
        if (user.isEmpty()) {
            throw new Exception("User not found");
        }
        
        User foundUser = user.get();
        
        if (!passwordEncoder.matches(request.getPassword(), foundUser.getPassword())) {
            throw new Exception("Invalid password");
        }
        
        String token = jwtUtil.generateToken(foundUser.getEmail());
        return new AuthResponse(token, foundUser.getEmail(), foundUser.getFullname());
    }
    
    public AuthResponse register(RegisterRequest request) throws Exception {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Email already registered");
        }
        
        if (request.getPassword().length() < 6) {
            throw new Exception("Password must be at least 6 characters");
        }
        
        User user = new User();
        user.setFullname(request.getFullname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);
        
        String token = jwtUtil.generateToken(savedUser.getEmail());
        return new AuthResponse(token, savedUser.getEmail(), savedUser.getFullname());
    }
    
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
