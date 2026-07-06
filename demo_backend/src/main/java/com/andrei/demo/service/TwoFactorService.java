package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.TwoFactorCode;
import com.andrei.demo.model.Verify2FADTO;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.TwoFactorCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.andrei.demo.model.LoginResponseDTO;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final TwoFactorCodeRepository twoFactorCodeRepository;
    private final JavaMailSender mailSender;
    private final JwtService jwtService;
    private final PersonRepository personRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${spring.mail.username}")
    private String mailFrom;

    public void generateCode(Person person) {

        String code =
                String.valueOf(
                        100000 + new Random().nextInt(900000)
                );

        TwoFactorCode entity = new TwoFactorCode();

        entity.setEmail(person.getEmail());
        entity.setCode(code);

        entity.setExpiresAt(
                LocalDateTime.now().plusMinutes(10)
        );

        entity.setUsed(false);

        twoFactorCodeRepository.save(entity);

        sendMail(
                person.getEmail(),
                code
        );
    }

    public LoginResponseDTO verify(Verify2FADTO dto) throws ValidationException {
        TwoFactorCode code = twoFactorCodeRepository
                .findTopByEmailAndCodeAndUsedFalseOrderByExpiresAtDesc(dto.getEmail(), dto.getCode())
                .orElseThrow(() -> new ValidationException("Invalid code"));

        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Code expired");
        }

        Person person = personRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ValidationException("User with email " + dto.getEmail() + " not found"));

        code.setUsed(true);
        twoFactorCodeRepository.save(code);

        String token = jwtService.generateToken(person);
        String refreshToken =
                refreshTokenService
                        .createRefreshToken(person)
                        .getToken();

        return new LoginResponseDTO(
                person.getId(),
                person.getName(),
                person.getEmail(),
                person.getRole(),
                person.getDepartament() != null
                        ? person.getDepartament().getId()
                        : null,
                token,
                refreshToken
        );
    }
    private void sendMail(
            String email,
            String code
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(mailFrom);

        message.setTo(email);

        message.setSubject(
                "Your login code"
        );

        message.setText(
                "Your verification code: "
                        + code
        );

        mailSender.send(message);
    }
}