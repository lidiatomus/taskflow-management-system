package com.andrei.demo.controller;

import com.andrei.demo.model.Departament;
import com.andrei.demo.model.DepartamentName;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.model.Task;
import com.andrei.demo.model.TaskStatus;
import com.andrei.demo.repository.DepartamentRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.TaskRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

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
        taskRepository.deleteAll();
        personRepository.deleteAll();
        departamentRepository.deleteAll();
    }

    private String bearerToken(Person person) {
        return "Bearer " + jwtService.generateToken(person);
    }

    @Test
    void getTasks_shouldReturnTasks_whenJwtIsPresent() throws Exception {
        Person member = new Person();
        member.setName("Maria");
        member.setEmail("maria@test.com");
        member.setPassword(passwordEncoder.encode("secret"));
        member.setAge(23);
        member.setRole(PersonRole.MEMBER);
        member = personRepository.save(member);

        Task task = new Task();
        task.setTitle("Telemetry dashboard");
        task.setDescription("Implement dashboard");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setDeadline(LocalDateTime.now().plusDays(1));
        task.setAssignedPersons(List.of(member));
        taskRepository.save(task);

        mockMvc.perform(get("/task")
                        .header("Authorization", bearerToken(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void create_shouldCreateTask_whenManagerSendsValidRequest() throws Exception {
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
        manager.setDepartament(dep);
        manager = personRepository.save(manager);

        Person member = new Person();
        member.setName("Maria");
        member.setEmail("maria@test.com");
        member.setPassword(passwordEncoder.encode("secret"));
        member.setAge(23);
        member.setRole(PersonRole.MEMBER);
        member.setDepartament(dep);
        member = personRepository.save(member);

        Map<String, Object> payload = Map.of(
                "title", "Telemetry dashboard",
                "description", "Implement dashboard",
                "personIds", List.of(member.getId().toString()),
                "status", "TODO",
                "deadline", LocalDateTime.now().plusDays(2).toString()
        );

        mockMvc.perform(post("/task")
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Telemetry dashboard"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void create_shouldReturnForbidden_whenJwtIsMissing() throws Exception {
        Map<String, Object> payload = Map.of(
                "title", "Telemetry dashboard",
                "description", "Implement dashboard",
                "personIds", List.of(),
                "status", "TODO",
                "deadline", LocalDateTime.now().plusDays(2).toString()
        );

        mockMvc.perform(post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void patch_shouldAllowMemberToChangeOnlyStatusOnOwnTask() throws Exception {
        Person member = new Person();
        member.setName("Maria");
        member.setEmail("maria@test.com");
        member.setPassword(passwordEncoder.encode("secret"));
        member.setAge(23);
        member.setRole(PersonRole.MEMBER);
        member = personRepository.save(member);

        Task task = new Task();
        task.setTitle("Telemetry dashboard");
        task.setDescription("Implement dashboard");
        task.setStatus(TaskStatus.TODO);
        task.setDeadline(LocalDateTime.now().plusDays(1));
        task.setAssignedPersons(List.of(member));
        task = taskRepository.save(task);

        Map<String, Object> payload = Map.of(
                "status", "IN_PROGRESS"
        );

        mockMvc.perform(patch("/task/" + task.getId())
                        .header("Authorization", bearerToken(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void delete_shouldDeleteTask_whenManagerDeletes() throws Exception {
        Person manager = new Person();
        manager.setName("Manager");
        manager.setEmail("manager@test.com");
        manager.setPassword(passwordEncoder.encode("secret"));
        manager.setAge(30);
        manager.setRole(PersonRole.TEAM_MANAGER);
        manager = personRepository.save(manager);

        Task task = new Task();
        task.setTitle("Telemetry dashboard");
        task.setDescription("Implement dashboard");
        task.setStatus(TaskStatus.TODO);
        task.setDeadline(LocalDateTime.now().plusDays(1));
        task.setAssignedPersons(List.of());
        task = taskRepository.save(task);

        mockMvc.perform(delete("/task/" + task.getId())
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isOk());
    }
}