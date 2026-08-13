package com.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Map<String, Object> user;
    private String message;
    
    public AuthResponse(String token, String email, String fullname) {
        this.token = token;
        this.user = new HashMap<>();
        this.user.put("email", email);
        this.user.put("fullname", fullname);
    }
}
