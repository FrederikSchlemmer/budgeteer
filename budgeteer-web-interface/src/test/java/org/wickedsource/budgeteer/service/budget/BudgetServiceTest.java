package org.wickedsource.budgeteer.service.budget;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.budget.BudgetTagEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.person.DailyRateRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.record.PlanRecordRepository;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.service.contract.ContractBaseData;
import org.wickedsource.budgeteer.service.contract.ContractDataMapper;

import java.util.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @InjectMocks
    private BudgetService budgetService;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private WorkRecordRepository workRecordRepository;
    @Mock
    private PlanRecordRepository planRecordRepository;
    @Mock
    private DailyRateRepository rateRepository;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private BudgetBaseDataMapper budgetBaseDataMapper;
    @Mock
    private ContractDataMapper contractDataMapper;

    @Test
    void testLoadBudgetBaseDataForProject() {
        when(budgetBaseDataMapper.map(Collections.singletonList(createBudgetEntity()))).thenReturn(Collections.singletonList(createBudgetBaseData()));
        when(budgetRepository.findByProjectIdOrderByNameAsc(1L)).thenReturn(Collections.singletonList(createBudgetEntity()));

        List<BudgetBaseData> budgets = budgetService.loadBudgetBaseDataForProject(1L);

        Assertions.assertThat(budgets)
                .contains(createBudgetBaseData())
                .hasSize(1);
    }

    @Test
    void testLoadBudgetBaseData() {
        when(budgetBaseDataMapper.map(createBudgetEntityOptional().get())).thenReturn(createBudgetBaseData());
        when(budgetRepository.findById(1L)).thenReturn(createBudgetEntityOptional());

        BudgetBaseData data = budgetService.loadBudgetBaseData(1L);

        Assertions.assertThat(data)
                .isEqualTo(createBudgetBaseData());
    }

    @Test
    void testLoadBudgetTags() {
        when(budgetRepository.getAllTagsInProject(1L)).thenReturn(Arrays.asList("1", "2"));

        List<String> loadedTags = budgetService.loadBudgetTags(1L);

        Assertions.assertThat(loadedTags)
                .containsAll(Arrays.asList("1", "2"))
                .hasSize(2);
    }

    @Test
    void testLoadBudgetDetailData() {
        when(budgetRepository.findById(1L)).thenReturn(createBudgetEntityOptional());
        when(workRecordRepository.getLatestWorkRecordDate(1L)).thenReturn(new Date());
        when(workRecordRepository.getSpentBudget(1L)).thenReturn(100000.0);
        when(planRecordRepository.getPlannedBudget(1L)).thenReturn(200000.0);
        when(workRecordRepository.getAverageDailyRate(1L)).thenReturn(50000.0);

        BudgetDetailData data = budgetService.loadBudgetDetailData(1L);

        Assertions.assertThat(data.getSpent().getAmountMinor().doubleValue())
                .isEqualTo(100000.0d, Assertions.within(1d));
        Assertions.assertThat(data.getUnplanned().getAmountMinor().doubleValue())
                .isEqualTo(-100000.0d, Assertions.within(1d));
        Assertions.assertThat(data.getAvgDailyRate().getAmountMinor().doubleValue())
                .isEqualTo(50000.0d, Assertions.within(1d));
    }

    @Test
    void testLoadBudgetsDetailData() {
        when(budgetRepository.findByAtLeastOneTag(1L, Arrays.asList("1", "2", "3"))).thenReturn(Collections.singletonList(createBudgetEntity()));
        when(workRecordRepository.getLatestWorkRecordDate(1L)).thenReturn(new Date());
        when(workRecordRepository.getSpentBudget(1L)).thenReturn(100000.0);
        when(planRecordRepository.getPlannedBudget(1L)).thenReturn(200000.0);
        when(workRecordRepository.getAverageDailyRate(1L)).thenReturn(50000.0);

        List<BudgetDetailData> data = budgetService.loadBudgetsDetailData(1L, new BudgetTagFilter(Arrays.asList("1", "2", "3"), 1L));

        Assertions.assertThat(data).hasSize(1);
        Assertions.assertThat(data.get(0).getSpent().getAmountMinor().doubleValue())
                .isEqualTo(100000.0d, Assertions.within(1d));
        Assertions.assertThat(data.get(0).getUnplanned().getAmountMinor().doubleValue())
                .isEqualTo(-100000.0d, Assertions.within(1d));
        Assertions.assertThat(data.get(0).getAvgDailyRate().getAmountMinor().doubleValue())
                .isEqualTo(50000.0d, Assertions.within(1d));
    }

    @Test
    void testLoadBudgetToEdit() {
        when(budgetRepository.findById(1L)).thenReturn(createBudgetEntityOptional());
        when(contractDataMapper.map(createContract())).thenReturn(new ContractBaseData());

        BudgetEntity budgetEntity = createBudgetEntity();
        EditBudgetData expectedData = new EditBudgetData();
        expectedData.setTotal(budgetEntity.getTotal());
        expectedData.setTags(mapEntitiesToTags(budgetEntity.getTags()));
        expectedData.setImportKey(budgetEntity.getImportKey());
        expectedData.setTitle(budgetEntity.getName());
        expectedData.setId(budgetEntity.getId());
        expectedData.setNote(budgetEntity.getNote());
        expectedData.setLimit(MoneyUtil.toMoneyNullsafe(null));
        expectedData.setProjectId(budgetEntity.getProject().getId());
        expectedData.setContract(new ContractBaseData());

        EditBudgetData data = budgetService.loadBudgetToEdit(1L);

        verify(budgetRepository, times(1)).findById(1L);
        Assertions.assertThat(data)
                .isEqualTo(expectedData);
    }

    @Test
    void testSaveBudget() {
        Optional<BudgetEntity> budgetEntityOptional = createBudgetEntityOptional();
        EditBudgetData data = getEditBudgetEntity();

        when(budgetRepository.findById(1L)).thenReturn(budgetEntityOptional);
        when(budgetRepository.save(any())).thenReturn(budgetEntityOptional.get());

        long returnedId = budgetService.saveBudget(data);

        Assertions.assertThat(returnedId)
                .isEqualTo(1L);
        verify(budgetRepository, times(1)).save(any());
    }

    @Test
    void testSaveBudgetWithContract() {
        Optional<BudgetEntity> budgetEntityOptional = createBudgetEntityOptional();

        when(budgetRepository.findById(1L)).thenReturn(createBudgetEntityOptional());
        when(contractRepository.findById(1L)).thenReturn(Optional.of(createContract()));
        when(budgetRepository.save(any())).thenReturn(budgetEntityOptional.get());

        EditBudgetData data = new EditBudgetData();
        data.setId(1L);
        ContractBaseData contractBaseData = new ContractBaseData();
        contractBaseData.setContractId(1L);
        data.setContract(contractBaseData);

        long returnedId = budgetService.saveBudget(data);

        Assertions.assertThat(returnedId)
                .isEqualTo(1L);
        verify(budgetRepository, times(1)).save(any());
        verify(contractRepository, times(1)).findById(1L);
    }

    @Test
    void testLoadBudgetUnits() {
        when(rateRepository.getDistinctRatesInCents(1L)).thenReturn(
                Arrays.asList(MoneyUtil.createMoney(100d),
                        MoneyUtil.createMoney(200d)));
        List<Double> units = budgetService.loadBudgetUnits(1L);

        Assertions.assertThat(units).hasSize(3)
                .containsAll(Arrays.asList(1d, 100d, 200d));
    }

    @Test
    void shouldThrowExceptionWhenConstraintIsViolated() {
        given(budgetRepository.save(Mockito.any(BudgetEntity.class)))
                .willThrow(new DataIntegrityViolationException("constraint violation"));
        when(budgetRepository.findById(1L)).thenReturn(createBudgetEntityOptional());

        Assertions.assertThatThrownBy(() -> budgetService.saveBudget(createBudgetEditEntity()), "Constraint Violation!")
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("constraint violation");
    }

    private BudgetBaseData createBudgetBaseData() {
        BudgetBaseData budgetBaseData = new BudgetBaseData();
        budgetBaseData.setId(1L);
        budgetBaseData.setName("Budget 123");
        return budgetBaseData;
    }

    private BudgetEntity createBudgetEntity() {
        BudgetEntity budget = new BudgetEntity();
        budget.setId(1L);
        budget.setTotal(MoneyUtil.createMoneyFromCents(100000));
        budget.setName("Budget 123");
        budget.getTags().add(new BudgetTagEntity("Tag1"));
        budget.getTags().add(new BudgetTagEntity("Tag2"));
        budget.getTags().add(new BudgetTagEntity("Tag3"));
        ProjectEntity project = new ProjectEntity();
        project.setId(1);
        budget.setProject(project);
        budget.setImportKey("budget123");
        return budget;
    }

    private Optional<BudgetEntity> createBudgetEntityOptional() {
        BudgetEntity budget = new BudgetEntity();
        budget.setId(1L);
        budget.setTotal(MoneyUtil.createMoneyFromCents(100000));
        budget.setName("Budget 123");
        budget.getTags().add(new BudgetTagEntity("Tag1"));
        budget.getTags().add(new BudgetTagEntity("Tag2"));
        budget.getTags().add(new BudgetTagEntity("Tag3"));
        budget.setContract(createContract());
        ProjectEntity project = new ProjectEntity();
        project.setId(1);
        budget.setProject(project);
        budget.setImportKey("budget123");
        return Optional.of(budget);
    }

    private EditBudgetData createBudgetEditEntity() {
        EditBudgetData data = new EditBudgetData();
        data.setId(1L);
        data.setImportKey("budget123");
        data.setTags(Arrays.asList("1", "2"));
        data.setTitle("title");
        data.setTotal(MoneyUtil.createMoneyFromCents(123));
        return data;
    }

    private EditBudgetData getEditBudgetEntity() {
        EditBudgetData data = new EditBudgetData();
        data.setId(1L);
        data.setImportKey("import");
        data.setTags(Arrays.asList("1", "2"));
        data.setTitle("title");
        data.setTotal(MoneyUtil.createMoneyFromCents(123));
        return data;
    }


    private ContractEntity createContract() {
        ContractEntity entity = new ContractEntity();
        entity.setId(1);
        entity.setName("TestName");
        entity.setBudgets(new LinkedList<>());
        return entity;
    }

    private List<String> mapEntitiesToTags(List<BudgetTagEntity> tagEntities) {
        List<String> tags = new ArrayList<>();
        for (BudgetTagEntity entity : tagEntities) {
            tags.add(entity.getTag());
        }
        return tags;
    }
}
