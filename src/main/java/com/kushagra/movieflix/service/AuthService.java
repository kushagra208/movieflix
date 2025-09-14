package com.kushagra.movieflix.service;

import com.kushagra.movieflix.dto.CustomResponse;
import com.kushagra.movieflix.entity.Role;
import com.kushagra.movieflix.entity.User;
import com.kushagra.movieflix.exception.BadRequestException;
import com.kushagra.movieflix.exception.NotFoundException;
import com.kushagra.movieflix.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.kushagra.movieflix.constants.ApiConstants.TOKEN_PREFIX;
import static com.kushagra.movieflix.constants.ApiConstants.USER_CACHE_PREFIX;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private JwtService jwtService;

    @Transactional
    public CustomResponse signup(User user) {
        log.info("Entered into method signup in the AuthService");
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);
        user = userRepository.save(user);
        redisTemplate.delete(USER_CACHE_PREFIX + user.getUsername()); // invalidate cache

        return CustomResponse.builder()
                .statusCode(String.valueOf(HttpStatus.CREATED.value()))
                .result(user)
                .status("success")
                .message("User registered successfully")
                .build();

    }

    public CustomResponse login(String username, String password) {
        log.info("Entered into method login in the AuthService");
        User user = getUserFromCache(username);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        String key = TOKEN_PREFIX + username;
        redisTemplate.opsForValue().set(key, token, jwtService.getExpirationMs(), TimeUnit.MILLISECONDS);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);

        return CustomResponse.builder()
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .result(result)
                .status("success")
                .message("User logged in successfully")
                .build();
    }

    public CustomResponse logout() {
        log.info("Entered into method logout in the AuthService");
        String token = httpServletRequest.getHeader("Authorization");

        if (token.contains("Bearer")) {
            token = token.split(" ")[1];
        } else {
            throw new BadRequestException("Unauthorised access");
        }

        String username = jwtService.extractUsername(token);

        String key = TOKEN_PREFIX + username;

        if (redisTemplate.hasKey(key)) {
            redisTemplate.delete(key);
            return CustomResponse.builder()
                    .message("User logged out successfully")
                    .statusCode(String.valueOf(HttpStatus.OK.value()))
                    .status("success")
                    .result(null)
                    .build();
        }

        throw new BadRequestException("User not logged in or already logged out");
    }

    public boolean isTokenValidInRedis(String username, String token) {
        String key = TOKEN_PREFIX + username;
        String storedToken = (String) redisTemplate.opsForValue().get(key);
        return storedToken != null && storedToken.equals(token);
    }

    private User getUserFromCache(String username) {
        log.info("Entered into getUserFromCache method in AuthService");

        String key = USER_CACHE_PREFIX + username;
        User cachedUser = (User) redisTemplate.opsForValue().get(key);

        if (cachedUser != null) {
            return cachedUser;
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        redisTemplate.opsForValue().set(key, user, 1, TimeUnit.HOURS); // cache for 1 hr
        return user;
    }
}

