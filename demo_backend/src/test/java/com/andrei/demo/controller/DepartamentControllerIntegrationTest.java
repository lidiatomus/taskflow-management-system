package com.andrei.demo.controller;

import com.andrei.demo.model.Departament;
import com.andrei.demo.model.DepartamentName;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.DepartamentRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.service.JwtService;
import org.springframework.test.context.ActiveProfiles;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DepartamentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartamentRepository departamentRepository;

    @Autowired
    private PersonRepository personRepository;

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
    void getDepartaments_shouldReturnAll_whenJwtIsPresent() throws Exception {
        Departament dep = new Departament();
        dep.setName(DepartamentName.values()[0]);
        dep.setDescription("Department test description");
        departamentRepository.save(dep);

        Person manager = new Person();
        manager.setName("Manager");
        manager.setEmail("manager@test.com");
        manager.setPassword(passwordEncoder.encode("secret"));
        manager.setAge(30);
        manager.setRole(PersonRole.TEAM_MANAGER);
        manager = personRepository.save(manager);

        mockMvc.perform(get("/departament")
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void create_shouldCreateDepartament_whenManagerSendsValidRequest() throws Exception {
        Person manager = new Person();
        manager.setName("Manager");
        manager.setEmail("manager@test.com");
        manager.setPassword(passwordEncoder.encode("secret"));
        manager.setAge(30);
        manager.setRole(PersonRole.TEAM_MANAGER);
        manager = personRepository.save(manager);

        Map<String, Object> payload = Map.of(
                "name", DepartamentName.values()[0].name(),
                "description", "Created from test"
        );

        mockMvc.perform(post("/departament")
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Created from test"));
    }

    @Test
    void patch_shouldUpdateOwnDepartment_whenDepartmentHeadEditsIt() throws Exception {
        Departament dep = new Departament();
        dep.setName(DepartamentName.values()[0]);
        dep.setDescription("Old description");
        dep = departamentRepository.save(dep);

        Person head = new Person();
        head.setName("Head");
        head.setEmail("head@test.com");
        head.setPassword(passwordEncoder.encode("secret"));
        head.setAge(35);
        head.setRole(PersonRole.DEPARTMENT_HEAD);
        head.setDepartament(dep);
        head = personRepository.save(head);

        Map<String, Object> payload = Map.of(
                "description", "New description"
        );

        mockMvc.perform(patch("/departament/" + dep.getId())
                        .header("Authorization", bearerToken(head))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("New description"));
    }

    @Test
    void delete_shouldReturnForbidden_whenJwtIsMissing() throws Exception {
        Departament dep = new Departament();
        dep.setName(DepartamentName.values()[0]);
        dep.setDescription("Department test description");
        dep = departamentRepository.save(dep);

        mockMvc.perform(delete("/departament/" + dep.getId()))
                .andExpect(status().isForbidden());
    }
}