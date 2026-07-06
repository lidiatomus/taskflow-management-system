package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Departament;
import com.andrei.demo.model.DepartamentCreateDTO;
import com.andrei.demo.model.DepartamentName;
import com.andrei.demo.model.DepartamentPatchDTO;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.DepartamentRepository;
import com.andrei.demo.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartamentServiceTest {

    @Mock
    private DepartamentRepository departamentRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private DepartamentService departamentService;

    @Test
    void getDepartaments_shouldReturnAll_whenUserCanView() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        Person user = new Person();
        List<Departament> deps = List.of(new Departament(), new Departament());

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(user);
        when(departamentRepository.findAll()).thenReturn(deps);

        List<Departament> result = departamentService.getDepartaments(loggedUserId);

        assertEquals(2, result.size());
        verify(authorizationService).canViewDepartament(user);
        verify(departamentRepository).findAll();
    }

    @Test
    void getDepartamentById_shouldReturnDepartament_whenExists() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Person user = new Person();
        Departament dep = new Departament();
        dep.setId(depId);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(user);
        when(departamentRepository.findById(depId)).thenReturn(Optional.of(dep));

        Departament result = departamentService.getDepartamentById(loggedUserId, depId);

        assertEquals(depId, result.getId());
        verify(authorizationService).canViewDepartament(user);
    }

    @Test
    void getDepartamentById_shouldThrow_whenNotFound() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Person user = new Person();

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(user);
        when(departamentRepository.findById(depId)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> departamentService.getDepartamentById(loggedUserId, depId)
        );

        assertEquals("Departament with id " + depId + " not found", ex.getMessage());
    }

    @Test
    void addDepartament_shouldSaveDepartament_whenManagerCreatesValidOne() throws Exception {
        UUID loggedUserId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        DepartamentCreateDTO dto = new DepartamentCreateDTO();
        dto.setName(DepartamentName.SOFTWARE);
        dto.setDescription("Software department");

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(departamentRepository.findByName(DepartamentName.SOFTWARE)).thenReturn(Optional.empty());
        when(departamentRepository.save(any(Departament.class))).thenAnswer(inv -> inv.getArgument(0));

        Departament result = departamentService.addDepartament(loggedUserId, dto);

        assertEquals(DepartamentName.SOFTWARE, result.getName());
        assertEquals("Software department", result.getDescription());
        verify(authorizationService).canCreateDepartament(manager);
        verify(departamentRepository).save(any(Departament.class));
    }

    @Test
    void addDepartament_shouldThrow_whenNameAlreadyExists() throws Exception {
        UUID loggedUserId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Departament existing = new Departament();
        existing.setName(DepartamentName.SOFTWARE);

        DepartamentCreateDTO dto = new DepartamentCreateDTO();
        dto.setName(DepartamentName.SOFTWARE);
        dto.setDescription("Software department");

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(departamentRepository.findByName(DepartamentName.SOFTWARE)).thenReturn(Optional.of(existing));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> departamentService.addDepartament(loggedUserId, dto)
        );

        assertEquals("Departament name already exists", ex.getMessage());
        verify(departamentRepository, never()).save(any());
    }

    @Test
    void updateDepartment_shouldSaveUpdatedDepartment_whenAllowed() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Departament existing = new Departament();
        existing.setId(depId);
        existing.setName(DepartamentName.SOFTWARE);

        DepartamentCreateDTO dto = new DepartamentCreateDTO();
        dto.setName(DepartamentName.MANAGEMENT);
        dto.setDescription("Updated description");

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(departamentRepository.findById(depId)).thenReturn(Optional.of(existing));
        when(departamentRepository.findByName(DepartamentName.MANAGEMENT)).thenReturn(Optional.empty());
        when(departamentRepository.save(any(Departament.class))).thenAnswer(inv -> inv.getArgument(0));

        Departament result = departamentService.updateDepartment(loggedUserId, depId, dto);

        assertEquals(DepartamentName.MANAGEMENT, result.getName());
        assertEquals("Updated description", result.getDescription());
        verify(authorizationService).canEditDepartament(manager, existing);
    }

    @Test
    void updateDepartment_shouldThrow_whenDuplicateNameExists() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Departament existing = new Departament();
        existing.setId(depId);
        existing.setName(DepartamentName.SOFTWARE);

        Departament other = new Departament();
        other.setName(DepartamentName.MANAGEMENT);

        DepartamentCreateDTO dto = new DepartamentCreateDTO();
        dto.setName(DepartamentName.MANAGEMENT);
        dto.setDescription("Updated description");

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(departamentRepository.findById(depId)).thenReturn(Optional.of(existing));
        when(departamentRepository.findByName(DepartamentName.MANAGEMENT)).thenReturn(Optional.of(other));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> departamentService.updateDepartment(loggedUserId, depId, dto)
        );

        assertEquals("Department name already exists", ex.getMessage());
    }

    @Test
    void patchDepartament_shouldPatchFields_whenAllowed() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Person head = new Person();
        head.setRole(PersonRole.DEPARTMENT_HEAD);

        Departament existing = new Departament();
        existing.setId(depId);
        existing.setName(DepartamentName.SOFTWARE);
        existing.setDescription("Old description");

        DepartamentPatchDTO dto = new DepartamentPatchDTO();
        dto.setDescription("New description");

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(head);
        when(departamentRepository.findById(depId)).thenReturn(Optional.of(existing));
        when(departamentRepository.save(any(Departament.class))).thenAnswer(inv -> inv.getArgument(0));

        Departament result = departamentService.patchDepartament(loggedUserId, depId, dto);

        assertEquals("New description", result.getDescription());
        verify(authorizationService).canEditDepartament(head, existing);
    }

    @Test
    void deleteDepartament_shouldNullMembersAndDelete_whenManagerDeletes() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        Person member1 = new Person();
        Person member2 = new Person();

        Departament dep = new Departament();
        dep.setId(depId);
        dep.setMembers(new ArrayList<>(List.of(member1, member2)));

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(departamentRepository.findById(depId)).thenReturn(Optional.of(dep));

        departamentService.deleteDepartament(loggedUserId, depId);

        assertNull(member1.getDepartament());
        assertNull(member2.getDepartament());

        verify(authorizationService).canDeleteDepartament(manager);
        verify(personRepository).saveAll(dep.getMembers());
        verify(departamentRepository).delete(dep);
    }

    @Test
    void deleteDepartament_shouldThrow_whenNotFound() throws Exception {
        UUID loggedUserId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();

        Person manager = new Person();
        manager.setRole(PersonRole.TEAM_MANAGER);

        when(authorizationService.requireLoggedUser(loggedUserId)).thenReturn(manager);
        when(departamentRepository.findById(depId)).thenReturn(Optional.empty());

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> departamentService.deleteDepartament(loggedUserId, depId)
        );

        assertEquals("Departament with id " + depId + " not found", ex.getMessage());
        verify(departamentRepository, never()).delete(any());
    }
}