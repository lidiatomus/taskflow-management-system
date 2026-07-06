package com.andrei.demo.controller;

import com.andrei.demo.model.Departament;
import com.andrei.demo.model.DepartamentName;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.DepartamentRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PersonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DepartamentRepository departamentRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        departamentRepository.deleteAll();
    }

    private String bearerToken(Person person) {
        return "Bearer " + jwtService.generateToken(person);
    }

    @Test
    void getAll_shouldReturnPeople_whenJwtIsPresent() throws Exception {
        Person p1 = new Person();
        p1.setName("Maria");
        p1.setEmail("maria@test.com");
        p1.setPassword(passwordEncoder.encode("pass"));
        p1.setAge(21);
        p1.setRole(PersonRole.MEMBER);

        Person p2 = new Person();
        p2.setName("Alex");
        p2.setEmail("alex@test.com");
        p2.setPassword(passwordEncoder.encode("pass"));
        p2.setAge(25);
        p2.setRole(PersonRole.TEAM_MANAGER);

        p1 = personRepository.save(p1);
        personRepository.save(p2);

        mockMvc.perform(get("/person")
                        .header("Authorization", bearerToken(p1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void create_shouldCreatePerson_whenManagerSendsValidRequest() throws Exception {
        Departament dep = new Departament();
        dep.setName(DepartamentName.values()[0]);
        dep.setDescription("Department test description");
        dep = departamentRepository.save(dep);

        Person manager = new Person();
        manager.setName("Manager");
        manager.setEmail("manager@test.com");
        manager.setPassword(passwordEncoder.encode("secret"));
        manager.setAge(30);
        manager.setRole(PersonRole.TEAM_MANAGER);
        manager = personRepository.save(manager);

        Map<String, Object> payload = Map.of(
                "name", "New Person",
                "email", "new.person@test.com",
                "password", "Password123!",
                "age", 22,
                "role", "MEMBER",
                "departamentId", dep.getId().toString()
        );

        mockMvc.perform(post("/person")
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Person"))
                .andExpect(jsonPath("$.email").value("new.person@test.com"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void patch_shouldUpdateOwnProfile_whenMemberPatchesAllowedFields() throws Exception {
        Person member = new Person();
        member.setName("Maria");
        member.setEmail("maria@test.com");
        member.setPassword(passwordEncoder.encode("secret"));
        member.setAge(23);
        member.setRole(PersonRole.MEMBER);
        member = personRepository.save(member);

        Map<String, Object> payload = Map.of(
                "name", "Maria Updated",
                "email", "maria.updated@test.com"
        );

        mockMvc.perform(patch("/person/" + member.getId())
                        .header("Authorization", bearerToken(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Updated"))
                .andExpect(jsonPath("$.email").value("maria.updated@test.com"));
    }

    @Test
    void delete_shouldReturnForbidden_whenJwtIsMissing() throws Exception {
        Person manager = new Person();
        manager.setName("Manager");
        manager.setEmail("manager@test.com");
        manager.setPassword(passwordEncoder.encode("secret"));
        manager.setAge(30);
        manager.setRole(PersonRole.TEAM_MANAGER);
        manager = personRepository.save(manager);

        mockMvc.perform(delete("/person/" + manager.getId()))
                .andExpect(status().isForbidden());
    }
}