package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.LoginRequestDTO;
import com.andrei.demo.model.LoginStepResponseDTO;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private AuthService authService;


    @Test
    void login_shouldSend2FACode_whenCredentialsAreValid()
            throws Exception {

        authService = new AuthService(
                personRepository,
                passwordEncoder,
                jwtService,
                twoFactorService,
                loginAttemptService,
                refreshTokenService
        );

        Person person = new Person();

        person.setName("Maria");
        person.setEmail("maria@test.com");

        person.setPassword(
                passwordEncoder.encode("secret")
        );

        LoginRequestDTO dto =
                new LoginRequestDTO();

        dto.setEmail("maria@test.com");
        dto.setPassword("secret");

        when(personRepository
                .findByEmail("maria@test.com"))
                .thenReturn(Optional.of(person));

        LoginStepResponseDTO result =
                authService.login(dto);

        assertEquals(
                "2FA code sent to email",
                result.getMessage()
        );

        verify(loginAttemptService)
                .checkIfBlocked("maria@test.com");

        verify(loginAttemptService)
                .loginSucceeded("maria@test.com");

        verify(twoFactorService)
                .generateCode(person);
    }


    @Test
    void login_shouldThrow_whenUserDoesNotExist()
            throws Exception {

        authService = new AuthService(
                personRepository,
                passwordEncoder,
                jwtService,
                twoFactorService,
                loginAttemptService,
                refreshTokenService
        );

        LoginRequestDTO dto =
                new LoginRequestDTO();

        dto.setEmail("missing@test.com");
        dto.setPassword("secret");

        when(personRepository
                .findByEmail("missing@test.com"))
                .thenReturn(Optional.empty());

        ValidationException ex =
                assertThrows(
                        ValidationException.class,
                        () -> authService.login(dto)
                );

        assertEquals(
                "User with email missing@test.com not found",
                ex.getMessage()
        );

        verify(loginAttemptService)
                .checkIfBlocked("missing@test.com");

        verify(loginAttemptService, never())
                .loginFailed(anyString());

        verify(loginAttemptService, never())
                .loginSucceeded(anyString());

        verify(twoFactorService, never())
                .generateCode(any());
    }


    @Test
    void login_shouldThrow_whenPasswordIsWrong()
            throws Exception {

        authService = new AuthService(
                personRepository,
                passwordEncoder,
                jwtService,
                twoFactorService,
                loginAttemptService,
                refreshTokenService
        );

        Person person = new Person();

        person.setEmail("maria@test.com");

        person.setPassword(
                passwordEncoder.encode("correct")
        );

        LoginRequestDTO dto =
                new LoginRequestDTO();

        dto.setEmail("maria@test.com");
        dto.setPassword("wrong");

        when(personRepository
                .findByEmail("maria@test.com"))
                .thenReturn(Optional.of(person));

        ValidationException ex =
                assertThrows(
                        ValidationException.class,
                        () -> authService.login(dto)
                );

        assertEquals(
                "Wrong password",
                ex.getMessage()
        );

        verify(loginAttemptService)
                .checkIfBlocked("maria@test.com");

        verify(loginAttemptService)
                .loginFailed("maria@test.com");

        verify(loginAttemptService, never())
                .loginSucceeded(anyString());

        verify(twoFactorService, never())
                .generateCode(any());
    }

}