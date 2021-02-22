package org.wickedsource.budgeteer.service.person;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.person.DailyRateEntity;
import org.wickedsource.budgeteer.persistence.person.PersonEntity;
import org.wickedsource.budgeteer.persistence.person.PersonRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.record.WorkRecordEntity;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.service.DateRange;
import org.wickedsource.budgeteer.service.budget.BudgetBaseData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceIntegrationTest {
    @InjectMocks
    private PersonService personService;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private WorkRecordRepository workRecordRepository;

    private final DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Test
    void testLoadPersonWithRates() {
        ProjectEntity projectEntity = getProjectEntity();
        BudgetEntity budgetEntity = getBudgetEntity(projectEntity);
        PersonEntity personEntity = getPersonEntity(projectEntity);
        personEntity.setDailyRates(getDailyRateEntities(personEntity, budgetEntity));

        when(personRepository.findOneFetchDailyRates(2L)).thenReturn(personEntity);

        PersonWithRates person = personService.loadPersonWithRates(2L);

        Assertions.assertThat(person)
                .isEqualTo(getPersonWithRates());
    }

    @Test
    void testSavePersonWithRates() {
        ProjectEntity projectEntity = getProjectEntity();
        BudgetEntity budgetEntity = getBudgetEntity(projectEntity);
        PersonEntity personEntity = getPersonEntity(projectEntity);

        when(personRepository.findById(2L)).thenReturn(Optional.of(personEntity));
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budgetEntity));

        personService.savePersonWithRates(getPersonWithRates());

        personEntity.setDailyRates(getDailyRateEntities(personEntity, budgetEntity));
        verify(personRepository, times(1)).save(personEntity);
    }

    @Test
    void testEditPersonRate() {
        ProjectEntity projectEntity = getProjectEntity();
        BudgetEntity budgetEntity = getBudgetEntity(projectEntity);
        PersonEntity personEntity = getPersonEntity(projectEntity);
        personEntity.setDailyRates(new ArrayList<>(getDailyRateEntities(personEntity, budgetEntity)));

        when(personRepository.findOneFetchDailyRates(2L)).thenReturn(personEntity);
        when(personRepository.findById(2L)).thenReturn(Optional.of(personEntity));
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budgetEntity));

        //Load
        PersonWithRates person = personService.loadPersonWithRates(2L);

        //Save
        person.getRates().get(0).setRate(MoneyUtil.createMoney(100));
        personService.savePersonWithRates(person);

        //Test
        personEntity.setDailyRates(getDailyRateEntities(personEntity, budgetEntity));
        verify(personRepository, times(1)).save(personEntity);
    }

    @SneakyThrows
    @Test
    void testWarnAboutManuallyEditedRates() {
        PersonWithRates person = getPersonWithRates();
        WorkRecordEntity workRecordEntity = (WorkRecordEntity) new WorkRecordEntity()
                .setDate(format.parse("01.06.2015"))
                .setBudget(getBudgetEntity(getProjectEntity()))
                .setPerson(getPersonEntity(getProjectEntity()))
                .setDailyRate(MoneyUtil.ZERO);

        when(workRecordRepository.findManuallyEditedEntries(1L, format.parse("01.01.2015"), format.parse("31.12.2015")))
                .thenReturn(Collections.singletonList(workRecordEntity));

        List<String> warnings = personService.getOverlapWithManuallyEditedRecords(person, 1);


        System.out.println(warnings.get(0));
        Assertions.assertThat(warnings)
                .hasSize(1)
                .containsExactly("A work record in the range 01.01.15 00:00 - 31.12.15 00:00 (Exact Date and Amount: Mon Jun 01 00:00:00 CEST 2015, EUR 0.00) for budget \"Budget 1\" has already been edited manually and will not be overwritten.");
    }

    @SneakyThrows
    private PersonWithRates getPersonWithRates() {
        return new PersonWithRates()
                .setName("person2")
                .setPersonId(2L)
                .setImportKey("person2")
                .setDefaultDailyRate(null)
                .setRates(new ArrayList<>(Arrays.asList(
                        new PersonRate(MoneyUtil.createMoney(600),
                                new BudgetBaseData(1L, "Budget 1"),
                                new DateRange(format.parse("01.01.2015"), format.parse("31.12.2015"))),
                        new PersonRate(MoneyUtil.createMoney(500),
                                new BudgetBaseData(1L, "Budget 1"),
                                new DateRange(format.parse("01.01.2016"), format.parse("31.12.2016")))
                )));
    }

    private PersonEntity getPersonEntity(ProjectEntity projectEntity) {
        return new PersonEntity()
                .setId(2L)
                .setName("person2")
                .setImportKey("person2")
                .setDefaultDailyRate(null)
                .setProject(projectEntity);
    }

    private ProjectEntity getProjectEntity() {
        return new ProjectEntity()
                .setId(1L)
                .setName("project1")
                .setAuthorizedUsers(Collections.emptyList())
                .setProjectEnd(null)
                .setProjectStart(null)
                .setContractFields(Collections.emptySet());
    }

    private BudgetEntity getBudgetEntity(ProjectEntity projectEntity) {
        return new BudgetEntity()
                .setId(1L)
                .setName("Budget 1")
                .setTotal(MoneyUtil.createMoney(1000))
                .setProject(projectEntity)
                .setTags(Collections.emptyList())
                .setPlanRecords(Collections.emptyList())
                .setImportKey("budget1")
                .setContract(null);
    }

    @SneakyThrows
    private List<DailyRateEntity> getDailyRateEntities(PersonEntity personEntity, BudgetEntity budgetEntity) {
        return Arrays.asList(new DailyRateEntity()
                        .setId(200L)
                        .setDateEnd(format.parse("31.12.2015"))
                        .setDateStart(format.parse("01.01.2015"))
                        .setPerson(personEntity)
                        .setRate(MoneyUtil.createMoney(600))
                        .setBudget(budgetEntity),
                new DailyRateEntity()
                        .setId(300L)
                        .setDateEnd(format.parse("31.12.2016"))
                        .setDateStart(format.parse("01.01.2016"))
                        .setPerson(personEntity)
                        .setRate(MoneyUtil.createMoney(500))
                        .setBudget(budgetEntity)
        );
    }
}
