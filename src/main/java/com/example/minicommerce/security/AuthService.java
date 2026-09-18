package com.example.minicommerce.security;

import com.example.minicommerce.dto.auth.AuthResponse;
import com.example.minicommerce.dto.auth.LoginRequest;
import com.example.minicommerce.dto.auth.RefreshRequest;
import com.example.minicommerce.dto.auth.RegisterRequest;
import com.example.minicommerce.entity.RefreshToken;
import com.example.minicommerce.entity.User;
import com.example.minicommerce.enums.Role;
import com.example.minicommerce.exception.*;
import com.example.minicommerce.repository.RefreshTokenRepository;
import com.example.minicommerce.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitService rateLimitService;


    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder, RateLimitService rateLimitService){
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.rateLimitService = rateLimitService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest, String deviceId){

        if(userRepository.existsByUsername(registerRequest.getUsername())){
            throw new UsernameAlreadyExistsException("Bu username zaten kullanılıyor");
        }
        if(userRepository.existsByMail(registerRequest.getMail())){
            throw new MailAlreadyExistsException("Bu mail zaten kullanılıyor");
        }
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setMail(registerRequest.getMail());
        user.setPasswordHash(hashedPassword);
        user.setRole(Role.USER);
        try{
            userRepository.save(user);
        }catch(DataIntegrityViolationException ex){
            throw new RegistrationConflictException("Kullanıcı adı veya e-posta zaten kullanılıyor");
        }

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(jwtService.generateRefreshToken());
        refreshToken.setExpireDate(jwtService.getRefreshExpirationDate());
        refreshToken.setUser(user);

        refreshToken.setDeviceId(deviceId);
        refreshTokenRepository.save(refreshToken);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken.getToken());

        return(authResponse);
    }
    @Transactional
    public AuthResponse login(LoginRequest loginRequest, String deviceId) {


        if(!rateLimitService.tryAcquireLoginLock(loginRequest.getUsername())) {
            throw new TooManyRequestsException("Çok hızlı deneme yaptınız.");
        }


        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Kullanıcı veya şifre hatalı"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Kullanıcı veya şifre hatalı");
        }
        //rateLimitService.releaseLoginLock(loginRequest.getUsername());


        refreshTokenRepository.deleteByUserAndDeviceId(user, deviceId);

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(jwtService.generateRefreshToken());
        refreshToken.setExpireDate(jwtService.getRefreshExpirationDate());
        refreshToken.setUser(user);
        refreshToken.setDeviceId(deviceId);
        refreshTokenRepository.save(refreshToken);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken.getToken());
        return authResponse;
    }
    @Transactional
    public AuthResponse refresh(RefreshRequest refreshRequest){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshRequest.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token bulunamadı"));

        if(refreshToken.getExpireDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Refresh token süresi dolmuş");
        }
        User user = refreshToken.getUser();

        String accessToken = jwtService.generateAccessToken(user);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshRequest.getRefreshToken());

        return authResponse;
    }

    //TODO ACCESS HALA GEÇERLİ ? güvenlik açığı?
    @Transactional
    public void logout(User user, String deviceId){
        refreshTokenRepository.deleteByUserAndDeviceId(user, deviceId);
    }
    @Transactional
    public void logoutAll(User user){
        refreshTokenRepository.deleteAllByUser(user);
    }
}
