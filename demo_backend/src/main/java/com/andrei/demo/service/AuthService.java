package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.LoginRequestDTO;
import com.andrei.demo.model.LoginResponseDTO;
import com.andrei.demo.model.LoginStepResponseDTO;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@AllArgsConstructor
public class AuthService {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TwoFactorService twoFactorService;
    private final LoginAttemptService loginAttemptService;
    private final RefreshTokenService refreshTokenService;

    public LoginStepResponseDTO login(LoginRequestDTO loginRequestDTO)
            throws ValidationException {

        loginAttemptService.checkIfBlocked(loginRequestDTO.getEmail());

        Person person = personRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new ValidationException(
                        "User with email " + loginRequestDTO.getEmail() + " not found"
                ));

        if (!passwordEncoder.matches(
                loginRequestDTO.getPassword(),
                person.getPassword()
        )) {
            loginAttemptService.loginFailed(loginRequestDTO.getEmail());
            throw new ValidationException("Wrong password");
        }

        loginAttemptService.loginSucceeded(loginRequestDTO.getEmail());

        twoFactorService.generateCode(person);

        return new LoginStepResponseDTO("2FA code sent to email");
    }

    public LoginResponseDTO refreshToken(
            String refreshToken
    ) throws ValidationException {

        Person person =
                refreshTokenService
                        .validateRefreshToken(
                                refreshToken
                        );

        String accessToken =
                jwtService.generateToken(
                        person
                );

        return new LoginResponseDTO(

                person.getId(),

                person.getName(),

                person.getEmail(),

                person.getRole(),

                person.getDepartament()!=null
                        ?
                        person.getDepartament().getId()
                        :
                        null,

                accessToken,

                refreshToken
        );
    }

    public void logout(
            String refreshToken
    ) throws ValidationException {

        refreshTokenService
                .revokeRefreshToken(
                        refreshToken
                );
    }
}