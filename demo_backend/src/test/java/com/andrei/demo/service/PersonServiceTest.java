package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.*;
import com.andrei.demo.repository.DepartamentRepository;
import com.andrei.demo.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private DepartamentRepository departamentRepository;

    @Mock
    private AuthorizationService authorizationService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private PersonService personService;

    @BeforeEach
    void setUp() {
        personService = new PersonService(
                personRepository,
                departamentRepository,
                authorizationService,
                passwordEncoder
        );
    }

    @Test
    void getAll_shouldReturnAllPeople() {
        List<Person> people = List.of(new Person(), new Person());
        when(personRepository.findAll()).thenReturn(people);

        List<Person> result = personService.getAll();

        assertEquals(2, result.size());
        verify(personRepository).findAll();
    }

    @Test
    void getById_shouldReturnPerson_whenExists() throws Exception {
        UUID id = UUID.randomUUID();
        Person person = new Person();
        person.setId(id);

        when(personRepository.findById(id)).thenReturn(Optional.of(person));

        Person result = personService.getById(id);

        assertEquals(id, result.getId());
        verify(personRepository).findById(id);
    }

    @Test
    void getById_shouldThrow_whenPersonDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(personRepository.findById(id)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> personService.getById(id)
        );

        assertEquals("Person not found", ex.getMessage());
    }

    @Test
    void create_shouldSavePerson_whenManagerCreatesValidPerson() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Departament dep = new Departament();
        dep.setId(depId);

        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setName("Maria");
        dto.setEmail("maria@test.com");
        dto.setAge(23);
        dto.setRole(PersonRole.MEMBER);
        dto.setDepartamentId(depId);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(departamentRepository.findById(depId)).thenReturn(Optional.of(dep));
        when(personRepository.findByEmail("maria@test.com")).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

        Person result = personService.create(loggedUserId, dto);

        assertEquals("Maria", result.getName());
        assertEquals("maria@test.com", result.getEmail());
        assertEquals(23, result.getAge());
        assertEquals(PersonRole.MEMBER, result.getRole());
        assertEquals(depId, result.getDepartament().getId());
        assertTrue(passwordEncoder.matches("Password123!", result.getPassword()));

        verify(authorizationService).canCreatePerson(manager);
        verify(personRepository).save(any(Person.class));
    }

    @Test
    void create_shouldThrow_whenDepartmentDoesNotExist() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setName("Maria");
        dto.setEmail("maria@test.com");
        dto.setAge(23);
        dto.setRole(PersonRole.MEMBER);
        dto.setDepartamentId(depId);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(personRepository.findByEmail("maria@test.com")).thenReturn(Optional.empty());
        when(departamentRepository.findById(depId)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> personService.create(loggedUserId, dto)
        );

        assertEquals("Department not found", ex.getMessage());
        verify(personRepository, never()).save(any());
    }

    @Test
    void update_shouldSaveUpdatedPerson_whenValid() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Person loggedUser = new Person();
        loggedUser.setRole(PersonRole.TEAM_MANAGER);

        Person target = new Person();
        target.setId(personId);

        Departament dep = new Departament();
        dep.setId(depId);

        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setName("Updated");
        dto.setEmail("updated@test.com");
        dto.setAge(30);
        dto.setRole(PersonRole.DEPARTMENT_HEAD);
        dto.setDepartamentId(depId);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(loggedUser);
        when(personRepository.findById(personId)).thenReturn(Optional.of(target));
        when(personRepository.findByEmail("updated@test.com")).thenReturn(Optional.empty());
        when(departamentRepository.findById(depId)).thenReturn(Optional.of(dep));
        when(personRepository.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

        Person result = personService.update(loggedUserId, personId, dto);

        assertEquals("Updated", result.getName());
        assertEquals("updated@test.com", result.getEmail());
        assertEquals(30, result.getAge());
        assertEquals(PersonRole.DEPARTMENT_HEAD, result.getRole());
        assertEquals(depId, result.getDepartament().getId());

        verify(authorizationService).canEditPerson(loggedUser, target);
        verify(authorizationService).validatePersonFieldChanges(
                loggedUser, target, depId, PersonRole.DEPARTMENT_HEAD
        );
    }

    @Test
    void update_shouldThrow_whenTargetPersonNotFound() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        Person loggedUser = new Person();
        loggedUser.setRole(PersonRole.TEAM_MANAGER);

        PersonCreateDTO dto = new PersonCreateDTO();

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(loggedUser);
        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> personService.update(loggedUserId, personId, dto)
        );

        assertEquals("Person not found", ex.getMessage());
    }

    @Test
    void patch_shouldUpdateAllowedFields_whenValid() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Person target = new Person();
        target.setId(personId);
        target.setPassword("oldPasswordHash");

        PersonPatchDTO dto = new PersonPatchDTO();
        dto.setName("Patched");
        dto.setEmail("patched@test.com");
        dto.setAge(40);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(personRepository.findById(personId)).thenReturn(Optional.of(target));
        when(personRepository.findByEmail("patched@test.com")).thenReturn(Optional.empty());
        when(personRepository.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));

        Person result = personService.patch(loggedUserId, personId, dto);

        assertEquals("Patched", result.getName());
        assertEquals("patched@test.com", result.getEmail());
        assertEquals(40, result.getAge());
        assertEquals("oldPasswordHash", result.getPassword());
    }

    @Test
    void patch_shouldThrow_whenNonManagerChangesRoleOrDepartment() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Person member = new Person();
        member.setRole(PersonRole.MEMBER);
        member.setId(loggedUserId);

        Person target = new Person();
        target.setId(loggedUserId);

        PersonPatchDTO dto = new PersonPatchDTO();
        dto.setRole(PersonRole.TEAM_MANAGER);
        dto.setDepartamentId(depId);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(member);
        when(personRepository.findById(personId)).thenReturn(Optional.of(target));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> personService.patch(loggedUserId, personId, dto)
        );

        assertEquals("Cannot change role or department", ex.getMessage());
        verify(personRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeletePerson_whenManagerDeletesExistingPerson() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(personRepository.existsById(personId)).thenReturn(true);

        personService.delete(loggedUserId, personId);

        verify(authorizationService).canDeletePerson(manager);
        verify(personRepository).deleteById(personId);
    }

    @Test
    void delete_shouldThrow_whenPersonDoesNotExist() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(personRepository.existsById(personId)).thenReturn(false);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> personService.delete(loggedUserId, personId)
        );

        assertEquals("Person not found", ex.getMessage());
        verify(personRepository, never()).deleteById(any());
    }

    @Test
    void getPeopleByDepartamentId_shouldReturnPeople_whenDepartmentExists() throws Exception {
        UUID depId = UUID.randomUUID();
        List<Person> people = List.of(new Person(), new Person());

        when(departamentRepository.existsById(depId)).thenReturn(true);
        when(personRepository.findByDepartamentId(depId)).thenReturn(people);

        List<Person> result = personService.getPeopleByDepartamentId(depId);

        assertEquals(2, result.size());
        verify(personRepository).findByDepartamentId(depId);
    }

    @Test
    void getPeopleByDepartamentId_shouldThrow_whenDepartmentDoesNotExist() {
        UUID depId = UUID.randomUUID();
        when(departamentRepository.existsById(depId)).thenReturn(false);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> personService.getPeopleByDepartamentId(depId)
        );

        assertEquals("Department not found", ex.getMessage());
    }
}