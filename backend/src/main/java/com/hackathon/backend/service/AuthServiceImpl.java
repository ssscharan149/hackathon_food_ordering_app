package com.hackathon.backend.service;

import com.hackathon.backend.dto.request.LoginRequest;
import com.hackathon.backend.dto.request.SignupRequest;
import com.hackathon.backend.dto.response.AuthResponse;
import com.hackathon.backend.dto.response.MessageResponse;
import com.hackathon.backend.dto.response.UserInfoResponse;
import com.hackathon.backend.exceptions.ApiException;
import com.hackathon.backend.exceptions.ResourceNotFoundException;
import com.hackathon.backend.model.Role;
import com.hackathon.backend.model.Role.RoleName;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.RoleRepository;
import com.hackathon.backend.repository.UserRepository;
import com.hackathon.backend.security.JwtService;
import com.hackathon.backend.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public MessageResponse signup(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username is already taken");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already in use");
        }

        RoleName roleName = resolveRoleName(signupRequest.getRole());
        Role role = roleRepository.findByRoleName(roleName)
                .orElseGet(() -> createRole(roleName));

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setPhone(signupRequest.getPhone());
        user.setRole(role);

        userRepository.save(user);

        return new MessageResponse("User registered successfully");
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, toUserInfo(user));
    }

    public MessageResponse logout() {
        return new MessageResponse("Logout successful. Remove the token on the client side.");
    }

    public UserInfoResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BadCredentialsException("Authentication is required");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", authentication.getName()));
        return toUserInfo(user);
    }

    private RoleName resolveRoleName(String requestedRole) {
        if ("admin".equalsIgnoreCase(requestedRole)) {
            return RoleName.ADMIN;
        }
        return RoleName.USER;
    }

    private Role createRole(RoleName roleName) {
        Role role = new Role();
        role.setRoleName(roleName);
        return roleRepository.save(role);
    }

    private UserInfoResponse toUserInfo(User user) {
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().getRoleName().name());
        return response;
    }
}
