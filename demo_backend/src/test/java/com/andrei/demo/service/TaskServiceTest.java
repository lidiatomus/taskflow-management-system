package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.model.Task;
import com.andrei.demo.model.TaskCreateDTO;
import com.andrei.demo.model.TaskPatchDTO;
import com.andrei.demo.model.TaskStatus;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTasks_shouldReturnAllTasks() {
        List<Task> tasks = List.of(new Task(), new Task());
        when(taskRepository.findAllWithPersons()).thenReturn(tasks);

        List<Task> result = taskService.getTasks();

        assertEquals(2, result.size());
        verify(taskRepository).findAllWithPersons();
    }

    @Test
    void getTaskById_shouldReturnTask_whenExists() throws Exception {
        UUID taskId = UUID.randomUUID();
        Task task = new Task();
        task.setId(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(taskId);

        assertEquals(taskId, result.getId());
        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_shouldThrow_whenTaskDoesNotExist() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> taskService.getTaskById(taskId)
        );

        assertEquals("Task with id " + taskId + " not found", ex.getMessage());
    }

    @Test
    void addTask_shouldSaveTask_whenManagerCreatesValidTask() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        Person manager = new Person();
        manager.setId(loggedUserId);
        manager.setRole(PersonRole.TEAM_MANAGER);

        Person assigned = new Person();
        assigned.setId(personId);

        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("Telemetry dashboard");
        dto.setDescription("Implement dashboard");
        dto.setPersonIds(List.of(personId));
        dto.setStatus(TaskStatus.TODO);
        dto.setDeadline(LocalDateTime.now().plusDays(2));

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(personRepository.findById(personId)).thenReturn(Optional.of(assigned));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.addTask(loggedUserId, dto);

        assertEquals("Telemetry dashboard", result.getTitle());
        assertEquals("Implement dashboard", result.getDescription());
        assertEquals(TaskStatus.TODO, result.getStatus());
        assertEquals(1, result.getAssignedPersons().size());
        assertNull(result.getCompletedAt());

        verify(authorizationService).canCreateTask(eq(manager), anyList());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void addTask_shouldSetCompletedAt_whenStatusIsDone() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Person assigned = new Person();
        assigned.setId(personId);

        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("Done task");
        dto.setDescription("Already finished");
        dto.setPersonIds(List.of(personId));
        dto.setStatus(TaskStatus.DONE);
        dto.setDeadline(LocalDateTime.now().plusDays(1));

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(personRepository.findById(personId)).thenReturn(Optional.of(assigned));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.addTask(loggedUserId, dto);

        assertEquals(TaskStatus.DONE, result.getStatus());
        assertNotNull(result.getCompletedAt());
    }

    @Test
    void addTask_shouldThrow_whenAssignedPersonDoesNotExist() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("Task");
        dto.setDescription("Desc");
        dto.setPersonIds(List.of(personId));
        dto.setStatus(TaskStatus.TODO);
        dto.setDeadline(LocalDateTime.now().plusDays(1));

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> taskService.addTask(loggedUserId, dto)
        );

        assertEquals("Person with id " + personId + " not found", ex.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_shouldSaveUpdatedTask_whenManagerUpdatesValidTask() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Person assigned = new Person();
        assigned.setId(personId);

        Task existing = new Task();
        existing.setId(taskId);
        existing.setStatus(TaskStatus.TODO);
        existing.setAssignedPersons(new ArrayList<>());

        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("Updated title");
        dto.setDescription("Updated desc");
        dto.setPersonIds(List.of(personId));
        dto.setStatus(TaskStatus.IN_PROGRESS);
        dto.setDeadline(LocalDateTime.now().plusDays(3));

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));
        when(personRepository.findById(personId)).thenReturn(Optional.of(assigned));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.updateTask(loggedUserId, taskId, dto);

        assertEquals("Updated title", result.getTitle());
        assertEquals("Updated desc", result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(1, result.getAssignedPersons().size());

        verify(authorizationService).canEditTask(manager, existing);
        verify(taskRepository).save(existing);
    }

    @Test
    void updateTask_shouldThrow_whenTaskDoesNotExist() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("Updated title");
        dto.setDescription("Updated desc");
        dto.setPersonIds(List.of());
        dto.setStatus(TaskStatus.IN_PROGRESS);
        dto.setDeadline(LocalDateTime.now().plusDays(3));

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> taskService.updateTask(loggedUserId, taskId, dto)
        );

        assertEquals("Task with id " + taskId + " not found", ex.getMessage());
    }

    @Test
    void updateTask_shouldThrow_whenTryingToMoveDoneTaskBack() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Person assigned = new Person();
        assigned.setId(personId);

        Task existing = new Task();
        existing.setId(taskId);
        existing.setStatus(TaskStatus.DONE);
        existing.setAssignedPersons(new ArrayList<>());

        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("Updated title");
        dto.setDescription("Updated desc");
        dto.setPersonIds(List.of(personId));
        dto.setStatus(TaskStatus.TODO);
        dto.setDeadline(LocalDateTime.now().plusDays(3));

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));
        when(personRepository.findById(personId)).thenReturn(Optional.of(assigned));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> taskService.updateTask(loggedUserId, taskId, dto)
        );

        assertEquals("A completed task cannot be changed back to TODO or IN_PROGRESS", ex.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_shouldThrow_whenMemberAttemptsFullUpdate() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        Person member = new Person();
        member.setRole(PersonRole.MEMBER);

        Person assigned = new Person();
        assigned.setId(personId);

        Task existing = new Task();
        existing.setId(taskId);
        existing.setStatus(TaskStatus.TODO);
        existing.setAssignedPersons(new ArrayList<>());

        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("Updated title");
        dto.setDescription("Updated desc");
        dto.setPersonIds(List.of(personId));
        dto.setStatus(TaskStatus.IN_PROGRESS);
        dto.setDeadline(LocalDateTime.now().plusDays(3));

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(member);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existing));
        when(personRepository.findById(personId)).thenReturn(Optional.of(assigned));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> taskService.updateTask(loggedUserId, taskId, dto)
        );

        assertEquals("MEMBER cannot fully update tasks", ex.getMessage());
    }

    @Test
    void patchTask_shouldAddAndRemovePersons() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID existingPersonId = UUID.randomUUID();
        UUID newPersonId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Person existingPerson = new Person();
        existingPerson.setId(existingPersonId);

        Person newPerson = new Person();
        newPerson.setId(newPersonId);

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setStatus(TaskStatus.TODO);
        existingTask.setAssignedPersons(new ArrayList<>(List.of(existingPerson)));

        TaskPatchDTO dto = new TaskPatchDTO();
        dto.setAddPersonIds(List.of(newPersonId));
        dto.setRemovePersonIds(List.of(existingPersonId));
        dto.setTitle("Patched title");

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(personRepository.findById(newPersonId)).thenReturn(Optional.of(newPerson));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.patchTask(loggedUserId, taskId, dto);

        assertEquals("Patched title", result.getTitle());
        assertEquals(1, result.getAssignedPersons().size());
        assertEquals(newPersonId, result.getAssignedPersons().get(0).getId());
    }

    @Test
    void patchTask_shouldSetCompletedAt_whenStatusBecomesDone() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setStatus(TaskStatus.IN_PROGRESS);
        existingTask.setAssignedPersons(new ArrayList<>());

        TaskPatchDTO dto = new TaskPatchDTO();
        dto.setStatus(TaskStatus.DONE);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.patchTask(loggedUserId, taskId, dto);

        assertEquals(TaskStatus.DONE, result.getStatus());
        assertNotNull(result.getCompletedAt());
    }

    @Test
    void patchTask_shouldThrow_whenTryingToMoveDoneTaskBack() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setStatus(TaskStatus.DONE);
        existingTask.setAssignedPersons(new ArrayList<>());

        TaskPatchDTO dto = new TaskPatchDTO();
        dto.setStatus(TaskStatus.TODO);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> taskService.patchTask(loggedUserId, taskId, dto)
        );

        assertEquals("A completed task cannot be changed back to TODO or IN_PROGRESS", ex.getMessage());
    }

    @Test
    void deleteTask_shouldDelete_whenManagerDeletesExistingTask() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(taskRepository.existsById(taskId)).thenReturn(true);

        taskService.deleteTask(loggedUserId, taskId);

        verify(authorizationService).canDeleteTask(manager);
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void deleteTask_shouldThrow_whenTaskDoesNotExist() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(taskRepository.existsById(taskId)).thenReturn(false);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> taskService.deleteTask(loggedUserId, taskId)
        );

        assertEquals("Task with id " + taskId + " not found", ex.getMessage());
    }

    @Test
    void getOverdueTasks_shouldReturnRepositoryResult() {
        List<Task> tasks = List.of(new Task(), new Task());
        when(taskRepository.findByDeadlineBeforeAndStatusNot(any(LocalDateTime.class), eq(TaskStatus.DONE)))
                .thenReturn(tasks);

        List<Task> result = taskService.getOverdueTasks();

        assertEquals(2, result.size());
        verify(taskRepository).findByDeadlineBeforeAndStatusNot(any(LocalDateTime.class), eq(TaskStatus.DONE));
    }

    @Test
    void getInProgressTasks_shouldReturnRepositoryResult() {
        List<Task> tasks = List.of(new Task());
        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS)).thenReturn(tasks);

        List<Task> result = taskService.getInProgressTasks();

        assertEquals(1, result.size());
        verify(taskRepository).findByStatus(TaskStatus.IN_PROGRESS);
    }

    @Test
    void getTasksByPersonId_shouldReturnTasks_whenPersonExists() throws Exception {
        UUID personId = UUID.randomUUID();
        List<Task> tasks = List.of(new Task(), new Task());

        when(personRepository.existsById(personId)).thenReturn(true);
        when(taskRepository.findTasksByPersonId(personId)).thenReturn(tasks);

        List<Task> result = taskService.getTasksByPersonId(personId);

        assertEquals(2, result.size());
        verify(taskRepository).findTasksByPersonId(personId);
    }

    @Test
    void getTasksByPersonId_shouldThrow_whenPersonDoesNotExist() {
        UUID personId = UUID.randomUUID();
        when(personRepository.existsById(personId)).thenReturn(false);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> taskService.getTasksByPersonId(personId)
        );

        assertEquals("Person with id " + personId + " not found", ex.getMessage());
        verify(taskRepository, never()).findTasksByPersonId(any());
    }
}