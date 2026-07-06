package com.andrei.demo.controller;

import com.andrei.demo.model.Departament;
import com.andrei.demo.model.DepartamentName;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.DepartamentRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.service.JwtService;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DepartamentRepository departamentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        departamentRepository.deleteAll();
    }

    @Test
    void login_shouldReturnUserData_whenCredentialsAreValid() throws Exception {
        Departament dep = new Departament();
        dep.setName(DepartamentName.values()[0]);
        dep.setDescription("Department");
        dep = departamentRepository.save(dep);

        Person person = new Person();
        person.setName("Maria");
        person.setEmail("maria@test.com");
        person.setPassword(passwordEncoder.encode("secret")); // 🔥 IMPORTANT
        person.setAge(23);
        person.setRole(PersonRole.MEMBER);
        person.setDepartament(dep);
        person = personRepository.save(person);

        Map<String, Object> payload = Map.of(
                "email", "maria@test.com",
                "password", "secret"
        );

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(person.getId().toString()))
                .andExpect(jsonPath("$.name").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@test.com"))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.departamentId").value(dep.getId().toString()))
                .andExpect(jsonPath("$.token").exists()); // 🔥 JWT verificare
    }

    @Test
    void login_shouldReturn4xx_whenPasswordIsWrong() throws Exception {
        Person person = new Person();
        person.setName("Maria");
        person.setEmail("maria@test.com");
        person.setPassword(passwordEncoder.encode("correct")); // 🔥 IMPORTANT
        person.setAge(23);
        person.setRole(PersonRole.MEMBER);
        personRepository.save(person);

        Map<String, Object> payload = Map.of(
                "email", "maria@test.com",
                "password", "wrong"
        );

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is4xxClientError());
    }
}