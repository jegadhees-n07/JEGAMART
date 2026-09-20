package com.jdmart.service;

import com.jdmart.dto.*;
import com.jdmart.exception.BadRequestException;
import com.jdmart.exception.ResourceNotFoundException;
import com.jdmart.model.*;
import com.jdmart.repository.AddressRepository;
import com.jdmart.repository.CartRepository;
import com.jdmart.repository.RoleRepository;
import com.jdmart.repository.UserRepository;
import com.jdmart.security.CustomUserDetails;
import com.jdmart.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       CartRepository cartRepository,
                       AddressRepository addressRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public String registerUser(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and Confirm Password do not match");
        }

        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new BadRequestException("Email address is already in use. Please login or use another email.");
        }

        User user = new User(
                request.getFullName().trim(),
                request.getEmail().toLowerCase().trim(),
                request.getMobile().trim(),
                passwordEncoder.encode(request.getPassword())
        );

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);

        // Initialize empty cart for user
        Cart cart = new Cart(savedUser);
        cartRepository.save(cart);

        return "Registration successful. Please login.";
    }

    public AuthResponse loginUser(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication, request.isRememberMe());
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        return new AuthResponse(jwt, user.getId(), user.getFullName(), user.getEmail(), user.getMobile(), roles);
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new BadRequestException("User is not authenticated");
        }

        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile() {
        User user = getAuthenticatedUser();
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        List<AddressDto> addresses = user.getAddresses().stream()
                .map(this::mapAddressToDto)
                .collect(Collectors.toList());

        return new UserProfileDto(user.getId(), user.getFullName(), user.getEmail(), user.getMobile(), roles, addresses);
    }

    @Transactional
    public UserProfileDto updateProfile(UserProfileDto dto) {
        User user = getAuthenticatedUser();
        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName().trim());
        }
        if (dto.getMobile() != null && !dto.getMobile().isBlank()) {
            user.setMobile(dto.getMobile().trim());
        }
        User saved = userRepository.save(user);
        return getCurrentUserProfile();
    }

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        User user = getAuthenticatedUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public AddressDto addAddress(AddressDto dto) {
        User user = getAuthenticatedUser();

        Address address = new Address(
                dto.getFullName(),
                dto.getMobile(),
                dto.getHouseBuilding(),
                dto.getStreet(),
                dto.getCity(),
                dto.getState(),
                dto.getPincode()
        );
        address.setUser(user);
        address.setDefault(dto.isDefault() || user.getAddresses().isEmpty());

        Address saved = addressRepository.save(address);
        return mapAddressToDto(saved);
    }

    private AddressDto mapAddressToDto(Address a) {
        return new AddressDto(a.getId(), a.getFullName(), a.getMobile(), a.getHouseBuilding(), a.getStreet(), a.getCity(), a.getState(), a.getPincode(), a.isDefault());
    }
}
