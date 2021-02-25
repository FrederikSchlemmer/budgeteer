package org.wickedsource.budgeteer.service.budget;

import org.joda.money.BigMoney;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.budget.*;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.person.DailyRateRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;
import org.wickedsource.budgeteer.persistence.record.PlanRecordRepository;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.service.UnknownEntityException;
import org.wickedsource.budgeteer.service.contract.ContractDataMapper;
import org.wickedsource.budgeteer.service.notification.MissingContractForBudgetNotification;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.components.listMultipleChoiceWithGroups.OptionGroup;
import org.wickedsource.budgeteer.web.pages.budgets.exception.InvalidBudgetImportKeyAndNameException;
import org.wickedsource.budgeteer.web.pages.budgets.exception.InvalidBudgetImportKeyException;
import org.wickedsource.budgeteer.web.pages.budgets.exception.InvalidBudgetNameException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@Service
@Transactional
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetBaseDataMapper budgetBaseDataMapper;

    @Autowired
    private WorkRecordRepository workRecordRepository;

    @Autowired
    private PlanRecordRepository planRecordRepository;

    @Autowired
    private DailyRateRepository rateRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractDataMapper contractDataMapper;

    /**
     * Loads all Budgets that the given user is qualified for and returns base data about them.
     *
     * @param projectId ID of the project
     * @return list of all budgets the user is qualified for
     */
    @PreAuthorize("canReadProject(#projectId)")
    public List<BudgetBaseData> loadBudgetBaseDataForProject(long projectId) {
        List<BudgetEntity> budgets = budgetRepository.findByProjectIdOrderByNameAsc(projectId);
        return budgetBaseDataMapper.map(budgets);
    }

    /**
     * Loads the base data of a single budget from the database.
     *
     * @param budgetId ID of the budget to load.
     * @return base data of the specified budget.
     */
    @PreAuthorize("canReadBudget(#budgetId)")
    public BudgetBaseData loadBudgetBaseData(long budgetId) {
        BudgetEntity budget = budgetRepository.findById(budgetId).orElseThrow(RuntimeException::new);
        return budgetBaseDataMapper.map(budget);
    }

    private List<BudgetEntity> loadBudgetEntities(long projectId, BudgetTagFilter filter) {
        List<BudgetEntity> budgets;
        if (filter.getSelectedTags().isEmpty()) {
            budgets = budgetRepository.findByProjectIdOrderByNameAsc(projectId);
        } else {
            budgets = new ArrayList<>(new TreeSet<>(findBudgetWithAtLeastOneTag(projectId, filter.getSelectedTags())));
        }
        return budgets;
    }

    /**
     * Loads the base data of a single budget from the database.
     *
     * @param personId ID of the person for which the budget should be loaded.
     * @return base data of the specified budget.
     */
    @PreAuthorize("canReadPerson(#personId)")
    public List<BudgetBaseData> loadBudgetBaseDataByPersonId(long personId) {
        List<BudgetEntity> budget = findAllBudgetsByPersonId(personId);
        return budgetBaseDataMapper.map(budget);
    }

    /**
     * Loads all tags assigned to any budget of the given user.
     *
     * @param projectId ID of the project
     * @return all tags assigned to any budget of the given user.
     */
    @PreAuthorize("canReadProject(#projectId)")
    public List<String> loadBudgetTags(long projectId) {
        List<BudgetEntity> budgetEntities = budgetRepository.findByProjectId(projectId);
        return budgetEntities.stream()
                .map(BudgetEntity::getTags)
                .flatMap(List::stream)
                .filter(budgetTagEntity -> budgetTagEntity.getBudget().getProject().getId() == projectId)
                .map(BudgetTagEntity::getTag)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Searches for Budgets that are tagged with AT LEAST ONE of the given tags.
     *
     * @param tags the tags to search for
     * @return all Budgets that match the search criteria
     */
    private List<BudgetEntity> findBudgetWithAtLeastOneTag(long projectId, List<String> tags) {
        List<BudgetEntity> budgetEntities = budgetRepository.findByProjectId(projectId);
        return budgetEntities.stream()
                .flatMap(budgetEntity -> Stream.of(budgetEntity.getTags())
                        .flatMap(List::stream)
                        .filter(budgetTagEntity -> tags.contains(budgetTagEntity.getTag()))
                        .map(budgetTagEntity -> budgetEntity))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Searches for Budgets with a missing total and map it to MissingBudgetTotalBean.
     *
     * @param projectId ID of the project
     * @return MissingBudgetTotalBean with information of the budgets with missing totals
     */
    @PreAuthorize("canReadProject(#projectId)")
    public List<MissingBudgetTotalBean> findBudgetByProjectIdWithMissingTotal(long projectId) {
        List<BudgetEntity> budgetEntities = budgetRepository.findByProjectIdOrderByNameAsc(projectId);
        return budgetEntities.stream()
                .filter(budgetEntity -> budgetEntity.getTotal().isZero())
                .map(budgetEntity -> new MissingBudgetTotalBean(budgetEntity.getId(), budgetEntity.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Searches for Budgets with a missing contract and matching project id.
     *
     * @param projectId ID of the project
     * @return List<MissingContractForBudgetNotification> with information of the budgets with missing contracts
     */
    @PreAuthorize("canReadProject(#projectId)")
    public List<MissingContractForBudgetNotification> findBudgetsWithMissingContractAndProjectId(long projectId) {
        List<BudgetEntity> budgetEntities = budgetRepository.findByProjectId(projectId);
        return budgetEntities.stream()
                .filter(budgetEntity -> Objects.isNull(budgetEntity.getContract()))
                .map(budgetEntity -> new MissingContractForBudgetNotification(budgetEntity.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the TaxCoefficient defined by the contract of the budget. If the budget has no contract or
     * the contract no taxrate assigned, this method returns 1.0
     *
     * @param budgetId The primary key of the budget record at hand.
     * @return 1.0+taxRate/100
     */
    @PreAuthorize("canReadBudget(#budgetId)")
    public Double findTaxCoefficientByBudget(long budgetId) {
        Optional<BudgetEntity> budgetEntityOptional = budgetRepository.findById(budgetId);
        if (!budgetEntityOptional.isPresent()) {
            return null;
        }
        BudgetEntity budgetEntity = budgetEntityOptional.get();
        if (Objects.isNull(budgetEntity.getContract())){
            return 1.0;
        }
        return budgetEntity.getContract().getTaxRate().divide(BigDecimal.valueOf(100), RoundingMode.CEILING)
                .add(BigDecimal.ONE).doubleValue();
    }

    /**
     * Returns a MissingBudgetTotalBean object for the given Budget if it's budget total is zero.
     * Returns null, if the budget is not zero or if there is no budget present with the id!
     *
     * @param budgetId ID of the budget
     * @return MissingBudgetTotalBean with information of the budgets with missing totals
     */
    @PreAuthorize("canReadBudget(#budgetId)")
    public MissingBudgetTotalBean findBudgetByBudgetIdWithMissingTotal(long budgetId) {
        Optional<BudgetEntity> budgetEntityOptional = budgetRepository.findById(budgetId);
        if (!budgetEntityOptional.isPresent()) {
            return null;
        }

        BudgetEntity budgetEntity = budgetEntityOptional.get();
        if (!budgetEntity.getTotal().isZero()) {
            return null;
        }
        return new MissingBudgetTotalBean(budgetEntity.getId(), budgetEntity.getName());
    }

    /**
     * Returns a MissingBudgetTotalBean object for the given Budget if it's budget total is zero.
     * Returns null, if the budget is not zero or if there is no budget present with the id!
     *
     * @param budgetId ID of the budget
     * @return MissingBudgetTotalBean with information of the budgets with missing totals
     */
    @PreAuthorize("canReadBudget(#budgetId)")
    public MissingContractForBudgetNotification findMissingContractForBudget(long budgetId) {
        Optional<BudgetEntity> budgetEntityOptional = budgetRepository.findById(budgetId);
        if (!budgetEntityOptional.isPresent()) {
            return null;
        }

        BudgetEntity budgetEntity = budgetEntityOptional.get();
        if (Objects.nonNull(budgetEntity.getContract())) {
            return null;
        }
        return new MissingContractForBudgetNotification(budgetEntity.getId());
    }

    /**
     * Searches for Budgets with the matching project id and maps them to LimitReachedBean.
     *
     * @param projectId ID of the project
     * @return List of LimitReachedBean
     */
    @PreAuthorize("canReadProject(#projectId)")
    public List<LimitReachedBean> findBudgetsForProjectId(long projectId) {
        List<BudgetEntity> budgetEntities = budgetRepository.findByProjectIdOrderByNameAsc(projectId);
        return budgetEntities.stream()
                .map(budgetEntity -> new LimitReachedBean(budgetEntity.getId(), budgetEntity.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Searches for Budget with the matching id and reached limit and maps it to LimitReachedBean.
     * Returns null if budget isn't present or limit isn't reached!
     *
     * @param budgetId ID of the budget to load.
     * @return null or LimitReachedBean
     */
    @PreAuthorize("canReadBudget(#budgetId)")
    public LimitReachedBean findBudgetWithReachedLimit(long budgetId, Money spent) {
        Optional<BudgetEntity> budgetEntityOptional = budgetRepository.findById(budgetId);

        if (!budgetEntityOptional.isPresent()) {
            return null;
        }

        BudgetEntity budgetEntity = budgetEntityOptional.get();
        if (budgetEntity.getLimit().isGreaterThan(spent) && budgetEntity.getLimit().isZero()) {
            return null;
        }
        return new LimitReachedBean(budgetEntity.getId(), budgetEntity.getName());
    }

    /**
     * Loads the detail data of a single budget.
     *
     * @param budgetId ID of the budget to load.
     * @return detail data for the requested budget.
     */
    @PreAuthorize("canReadBudget(#budgetId)")
    public BudgetDetailData loadBudgetDetailData(long budgetId) {
        BudgetEntity budget = budgetRepository.findById(budgetId).orElseThrow(RuntimeException::new);
        return enrichBudgetEntity(budget);
    }

    private BudgetDetailData enrichBudgetEntity(BudgetEntity entity) {
        Date lastUpdated = workRecordRepository.getLatestWorkRecordDate(entity.getId());
        Double spentBudgetInCents = workRecordRepository.getSpentBudget(entity.getId());
        Double plannedBudgetInCents = planRecordRepository.getPlannedBudget(entity.getId());
        Double avgDailyRateInCents = workRecordRepository.getAverageDailyRate(entity.getId());
        Double taxCoefficient = findTaxCoefficientByBudget(entity.getId());

        BudgetDetailData data = new BudgetDetailData();
        data.setId(entity.getId());
        data.setLastUpdated(lastUpdated);
        data.setName(entity.getName());
        data.setDescription(entity.getDescription());
        data.setTags(mapEntitiesToTags(entity.getTags()));
        // Money
        data.setSpent(MoneyUtil.toMoneyNullsafe(spentBudgetInCents));
        data.setSpent_gross(data.getSpent().multipliedBy(taxCoefficient, RoundingMode.FLOOR));
        data.setTotal(entity.getTotal());
        data.setTotal_gross(data.getTotal().multipliedBy(taxCoefficient, RoundingMode.FLOOR));
        data.setAvgDailyRate(MoneyUtil.toMoneyNullsafe(avgDailyRateInCents));
        data.setAvgDailyRate_gross(data.getAvgDailyRate().multipliedBy(taxCoefficient, RoundingMode.FLOOR));
        data.setUnplanned(entity.getTotal().minus(MoneyUtil.toMoneyNullsafe(plannedBudgetInCents)));
        data.setUnplanned_gross(data.getUnplanned().multipliedBy(taxCoefficient, RoundingMode.FLOOR));
        data.setLimit(MoneyUtil.getMoneyNullsafe(entity.getLimit()));
        // Money end
        data.setContractName(entity.getContract() == null ? null : entity.getContract().getName());
        data.setContractId(entity.getContract() == null ? 0 : entity.getContract().getId());
        return data;
    }

    private List<String> mapEntitiesToTags(List<BudgetTagEntity> tagEntities) {
        List<String> tags = new ArrayList<String>();
        for (BudgetTagEntity entity : tagEntities) {
            tags.add(entity.getTag());
        }
        return tags;
    }

    private List<BudgetTagEntity> mapTagsToEntities(List<String> tags, BudgetEntity budget) {
        List<BudgetTagEntity> entities = new ArrayList<BudgetTagEntity>();
        if (tags != null) {
            for (String tag : tags) {
                BudgetTagEntity entity = new BudgetTagEntity();
                entity.setTag(tag);
                entity.setBudget(budget);
                entities.add(entity);
            }
        }
        return entities;
    }

    /**
     * Loads all budgets the given user has access to that match the given filter.
     *
     * @param projectId ID of the project
     * @param filter    the filter to apply when loading the budgets
     * @return list of budgets that match the filter.
     */
    @PreAuthorize("canReadProject(#projectId)")
    public List<BudgetDetailData> loadBudgetsDetailData(long projectId, BudgetTagFilter filter) {
        List<BudgetEntity> budgets = loadBudgetEntities(projectId, filter);
        List<BudgetDetailData> dataList = new ArrayList<BudgetDetailData>();
        for (BudgetEntity entity : budgets) {
            // TODO: 4 additional database queries per loop! These can yet be optimized to 4 queries total!
            dataList.add(enrichBudgetEntity(entity));
        }
        return dataList;
    }

    /**
     * Loads all budgets the given user has access to that match the tag and remaining filter.
     *
     * @param projectId       ID of the project
     * @param filter          the filter to apply when loading the budgets
     * @param remainingFilter budgets with values above this will be included
     * @return list of budgets that match the filter.
     */
    public List<BudgetDetailData> loadBudgetsDetailData(long projectId, BudgetTagFilter filter, Long remainingFilter) {
        List<BudgetDetailData> temp = loadBudgetsDetailData(projectId, filter);
        List<BudgetDetailData> result = new ArrayList<>();
        if (remainingFilter == 0) {
            return temp;
        }
        for (BudgetDetailData e : temp) {
            if (!BudgeteerSession.get().isTaxEnabled()) {
                if (e.getRemaining().isGreaterThan(() -> BigMoney.of(e.getRemaining().getCurrencyUnit(), new BigDecimal(remainingFilter)))) {
                    result.add(e);
                }
            } else {
                if (e.getRemaining_gross().isGreaterThan(() -> BigMoney.of(e.getRemaining_gross().getCurrencyUnit(), new BigDecimal(remainingFilter)))) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    /**
     * Loads the data of a budget to edit in the UI.
     *
     * @param budgetId ID of the budget whose data to load.
     * @return data object containing the data that can be changed in the UI.
     */
    @PreAuthorize("canReadBudget(#budgetId)")
    public EditBudgetData loadBudgetToEdit(long budgetId) {
        BudgetEntity budget = budgetRepository.findById(budgetId).orElseThrow(() -> new UnknownEntityException(BudgetEntity.class, budgetId));
        EditBudgetData data = new EditBudgetData();
        data.setId(budget.getId());
        data.setDescription(budget.getDescription());
        data.setTotal(budget.getTotal());
        data.setLimit(MoneyUtil.getMoneyNullsafe(budget.getLimit()));
        data.setTitle(budget.getName());
        data.setNote(budget.getNote());
        data.setTags(mapEntitiesToTags(budget.getTags()));
        data.setImportKey(budget.getImportKey());
        data.setProjectId(budget.getProject().getId());
        data.setContract(contractDataMapper.map(budget.getContract()));
        return data;
    }

    @PreAuthorize("canReadPerson(#personId)")
    public List<BudgetEntity> findAllBudgetsByPersonId(long personId) {
        Iterable<BudgetEntity> budgetEntities = budgetRepository.findAll();
        return StreamSupport.stream(budgetEntities.spliterator(), false)
                .flatMap(budgetEntity -> budgetEntity.getWorkRecords().stream()
                        .filter(workRecordEntity -> workRecordEntity.getPerson().getId() == personId)
                        .map(workRecordEntity -> budgetEntity))
                .collect(Collectors.toList());
    }

    /**
     * Stores the data to the given budget.
     *
     * @param data the data to store in the database
     * @return the
     */
    public long saveBudget(EditBudgetData data) {

        BudgetEntity budget = new BudgetEntity();
        if (data.getId() != 0) {
            budget = budgetRepository.findById(data.getId()).orElseThrow(RuntimeException::new);
        } else {
            ProjectEntity project = projectRepository.findById(data.getProjectId()).orElseThrow(RuntimeException::new);
            budget.setProject(project);
        }

        boolean duplicateImportKey = !Objects.equals(budget.getImportKey(), data.getImportKey()) && budgetRepository.existsByImportKeyAndProjectId(data.getImportKey(), data.getProjectId());
        boolean duplicateName = !Objects.equals(budget.getName(), data.getTitle()) && budgetRepository.existsByNameAndProjectId(data.getTitle(), data.getProjectId());

        if (duplicateImportKey && duplicateName) {
            throw new InvalidBudgetImportKeyAndNameException();
        }
        if (duplicateImportKey) {
            throw new InvalidBudgetImportKeyException();
        }
        if (duplicateName) {
            throw new InvalidBudgetNameException();
        }
        budget.setImportKey(data.getImportKey());
        budget.setDescription(data.getDescription());
        budget.setTotal(data.getTotal());
        budget.setLimit(data.getLimit());
        budget.setName(data.getTitle());
        budget.setNote(data.getNote());
        budget.getTags().clear();
        budget.getTags().addAll(mapTagsToEntities(data.getTags(), budget));
        if (data.getContract() == null) {
            budget.setContract(null);
        } else {
            ContractEntity contractEntity = contractRepository.findById(data.getContract().getContractId()).orElseThrow(RuntimeException::new);
            budget.setContract(contractEntity);
        }
        budgetRepository.save(budget);
        return budget.getId();
    }

    /**
     * Returns the units in which the user can have his budget values displayed. One unit is active, the others are
     * inactive.
     *
     * @param projectId ID of the project whose budgets to load
     * @return a list containing all available budget units, one of which is currently active.
     */
    @PreAuthorize("canReadProject(#projectId)")
    public List<Double> loadBudgetUnits(long projectId) {
        List<Double> units = new ArrayList<Double>();
        units.add(1d);
        List<Money> rates = rateRepository.getDistinctRatesInCents(projectId);
        for (Money rate : rates) {
            units.add(rate.getAmount().doubleValue());
        }
        return units;
    }

    @PreAuthorize("canReadBudget(#id)")
    public void deleteBudget(long id) {
        budgetRepository.deleteById(id);
    }

    @PreAuthorize("canReadContract(#cId)")
    public List<BudgetDetailData> loadBudgetByContract(long cId) {
        List<BudgetDetailData> result = new LinkedList<BudgetDetailData>();
        List<BudgetEntity> temp = budgetRepository.findByContractId(cId);
        if (temp != null) {
            for (BudgetEntity b : temp) {
                result.add(enrichBudgetEntity(b));
            }
        }
        return result;
    }

    public List<OptionGroup<BudgetBaseData>> getPossibleBudgetDataForPersonAndProject(long projectId, long personId) {

        List<BudgetBaseData> allBudgets = loadBudgetBaseDataForProject(BudgeteerSession.get().getProjectId());
        List<BudgetBaseData> personalBudgets = loadBudgetBaseDataForPerson(personId);

        allBudgets.removeAll(personalBudgets);

        List<OptionGroup<BudgetBaseData>> result = new LinkedList();
        if (!personalBudgets.isEmpty()) {
            result.add(new OptionGroup<>("Used", personalBudgets));
        }
        if (!allBudgets.isEmpty()) {
            result.add(new OptionGroup<>("All", allBudgets));
        }

        return result;
    }

    @PreAuthorize("canReadPerson(#personId)")
    private List<BudgetBaseData> loadBudgetBaseDataForPerson(long personId) {
        List<BudgetEntity> budgets = new ArrayList<>(new TreeSet<>(findAllBudgetsByPersonId(personId)));
        return budgetBaseDataMapper.map(budgets);
    }

    @PreAuthorize("canReadProject(#projectId)")
    public boolean projectHasBudgets(long projectId) {
        List<BudgetEntity> budgets = budgetRepository.findByProjectIdOrderByNameAsc(projectId);
        return (budgets != null && !budgets.isEmpty());
    }
}
