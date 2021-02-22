package org.wickedsource.budgeteer.service.project;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.imports.ImportRepository;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceRepository;
import org.wickedsource.budgeteer.persistence.person.DailyRateRepository;
import org.wickedsource.budgeteer.persistence.person.PersonRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;
import org.wickedsource.budgeteer.persistence.record.PlanRecordRepository;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.persistence.user.UserEntity;
import org.wickedsource.budgeteer.persistence.user.UserRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @InjectMocks
    private ProjectService projectService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private ImportRepository importRepository;
    @Mock
    private PlanRecordRepository planRecordRepository;
    @Mock
    private WorkRecordRepository workRecordRepository;
    @Mock
    private ProjectBaseDataMapper mapper;
    @Mock
    private DailyRateRepository dailyRateRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private ContractRepository contractRepository;

    @Test
    void testCreateProject() throws Exception {
        when(projectRepository.save(any(ProjectEntity.class))).thenReturn(createProjectEntity());
        when(userRepository.findById(anyLong())).thenReturn(createUserWithProjects());
        when(mapper.map(any(ProjectEntity.class))).thenCallRealMethod();

        ProjectBaseData project = projectService.createProject("MyProject", 1L);

        verify(projectRepository, times(1)).save(any(ProjectEntity.class));
        Assertions.assertThat(project.getName())
                .isEqualTo("name");
    }

    @Test
    void testGetProjectsForUser() {
        when(userRepository.findById(1L)).thenReturn(createUserWithProjects());
        when(mapper.map(anyList())).thenCallRealMethod();
        when(mapper.map(any(ProjectEntity.class))).thenCallRealMethod();

        List<ProjectBaseData> projects = projectService.getProjectsForUser(1L);

        Assertions.assertThat(projects)
                .hasSize(2);
    }

    @Test
    void testDeleteProject() {
        projectService.deleteProject(1L);

        verify(dailyRateRepository, times(1)).deleteByProjectId(1L);
        verify(planRecordRepository, times(1)).deleteByImportAndProjectId(1L);
        verify(workRecordRepository, times(1)).deleteByImportAndProjectId(1L);
        verify(budgetRepository, times(1)).deleteByProjectId(1L);
        verify(personRepository, times(1)).deleteByProjectId(1L);
        verify(invoiceRepository, times(1)).deleteInvoiceFieldByProjectId(1L);
        verify(invoiceRepository, times(1)).deleteContractInvoiceFieldByProject(1L);
        verify(invoiceRepository, times(1)).deleteByProjectId(1L);
        verify(contractRepository, times(1)).deleteContractFieldByProjectId(1L);
        verify(contractRepository, times(1)).deleteByProjectId(1L);

        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProjectWithUsers() {
        UserEntity userEntity = createUserWithProjects().get();
        ProjectEntity projectEntity = createProjectEntity();
        userEntity.setDefaultProject(projectEntity);
        projectEntity.setAuthorizedUsers(Collections.singletonList(userEntity));
        Optional<ProjectEntity> optionalProjectEntity = Optional.of(projectEntity);

        when(projectRepository.findById(1L)).thenReturn(optionalProjectEntity);

        projectService.deleteProject(1L);

        verify(dailyRateRepository, times(1)).deleteByProjectId(1L);
        verify(planRecordRepository, times(1)).deleteByImportAndProjectId(1L);
        verify(workRecordRepository, times(1)).deleteByImportAndProjectId(1L);
        verify(budgetRepository, times(1)).deleteByProjectId(1L);
        verify(personRepository, times(1)).deleteByProjectId(1L);
        verify(invoiceRepository, times(1)).deleteInvoiceFieldByProjectId(1L);
        verify(invoiceRepository, times(1)).deleteContractInvoiceFieldByProject(1L);
        verify(invoiceRepository, times(1)).deleteByProjectId(1L);
        verify(contractRepository, times(1)).deleteContractFieldByProjectId(1L);
        verify(contractRepository, times(1)).deleteByProjectId(1L);

        userEntity.setDefaultProject(null);
        verify(userRepository, times(1)).save(userEntity);

        verify(projectRepository, times(1)).deleteById(1L);
    }

    private Optional<UserEntity> createUserWithProjects() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("user");
        user.setPassword("password");
        user.getAuthorizedProjects().add(createProjectEntity());
        user.getAuthorizedProjects().add(createProjectEntity());
        return Optional.of(user);
    }

    private ProjectEntity createProjectEntity() {
        ProjectEntity project = new ProjectEntity();
        project.setId(1L);
        project.setName("name");
        return project;
    }
}
