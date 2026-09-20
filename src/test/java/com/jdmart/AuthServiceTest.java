package com.jdmart;

import com.jdmart.dto.AuthRequest;
import com.jdmart.dto.AuthResponse;
import com.jdmart.dto.RegisterRequest;
import com.jdmart.exception.BadRequestException;
import com.jdmart.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest(
                "Amit Kumar",
                "amit.kumar@test.com",
                "9876543219",
                "Password@123",
                "Password@123"
        );

        String result = authService.registerUser(request);
        assertEquals("Registration successful. Please login.", result);
    }

    @Test
    void testRegisterUser_PasswordMismatch() {
        RegisterRequest request = new RegisterRequest(
                "Amit Kumar",
                "amit.mismatch@test.com",
                "9876543219",
                "Password@123",
                "DifferentPassword"
        );

        assertThrows(BadRequestException.class, () -> authService.registerUser(request));
    }

    @Test
    void testLoginUser_Success() {
        // Admin is seeded by DatabaseSeeder
        AuthRequest request = new AuthRequest("admin@jdmart.com", "Admin@123");
        AuthResponse response = authService.loginUser(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("admin@jdmart.com", response.getEmail());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
    }
}
