package org.wickedsource.budgeteer.service.contract;

import org.apache.commons.lang3.StringUtils;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.contract.*;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceEntity;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectContractField;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;
import org.wickedsource.budgeteer.persistence.record.WorkRecordEntity;
import org.wickedsource.budgeteer.web.pages.contract.overview.table.ContractOverviewTableModel;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ContractDataMapper mapper;

    @PreAuthorize("canReadProject(#projectId)")
    public ContractOverviewTableModel getContractOverviewByProject(long projectId) {
        ContractOverviewTableModel result = new ContractOverviewTableModel();
        result.setContracts(mapper.map(contractRepository.findByProjectId(projectId)));
        return result;
    }

    @PreAuthorize("canReadContract(#contractId)")
    public ContractBaseData getContractById(long contractId) {
        return mapper.map(contractRepository.findById(contractId).orElse(null));
    }

    @PreAuthorize("canReadProject(#projectId)")
    public List<ContractBaseData> getContractsByProject(long projectId) {
        List<ContractEntity> contracts = new LinkedList<ContractEntity>();
        contracts.addAll(contractRepository.findByProjectId(projectId));
        return mapper.map(contracts);
    }

    @PreAuthorize("canReadProject(#projectId)")
    public ContractBaseData getEmptyContractModel(long projectId) {
        ProjectEntity project = projectRepository.findById(projectId).orElseThrow(RuntimeException::new);
        ContractBaseData model = new ContractBaseData(projectId);
        Set<ProjectContractField> fields = project.getContractFields();
        for (ProjectContractField field : fields) {
            model.getContractAttributes().add(new DynamicAttributeField(field.getFieldName(), ""));
        }
        return model;
    }

    public long save(ContractBaseData contractBaseData) {
        ProjectEntity project = projectRepository.findById(contractBaseData.getProjectId()).orElseThrow(RuntimeException::new);
        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setId(0);
        contractEntity.setProject(project);

        if (contractBaseData.getContractId() != 0) {
            contractEntity = contractRepository.findById(contractBaseData.getContractId()).orElseThrow(RuntimeException::new);
        }
        //Update basic information
        contractEntity.setName(contractBaseData.getContractName());
        contractEntity.setBudget(contractBaseData.getBudget());
        contractEntity.setInternalNumber(contractBaseData.getInternalNumber());
        contractEntity.setStartDate(contractBaseData.getStartDate());
        contractEntity.setType(contractBaseData.getType());
        contractEntity.setLink(contractBaseData.getFileModel().getLink());
        contractEntity.setFileName(contractBaseData.getFileModel().getFileName());
        contractEntity.setFile(contractBaseData.getFileModel().getFile());
        if (contractBaseData.getTaxRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Taxrate must be positive.");
        } else {
            contractEntity.setTaxRate(contractBaseData.getTaxRate());
        }

        // Use LinkedHashMap as backing map implementation to ensure insertion order
        Map<String, ContractFieldEntity> contractFields = contractEntity.getContractFields().stream()
                .collect(Collectors.toMap(field -> field.getField().getFieldName(), Function.identity(), (a, b) -> a,
                        LinkedHashMap::new));

        for (DynamicAttributeField dynamicAttribute : contractBaseData.getContractAttributes()) {
            ContractFieldEntity fieldEntity = contractFields.get(dynamicAttribute.getName().trim());
            if (fieldEntity != null) {
                fieldEntity.setValue(StringUtils.trimToEmpty(dynamicAttribute.getValue()));
            } else {
                ContractFieldEntity newFieldEntity = createNewContractField(contractBaseData, dynamicAttribute, project);
                contractEntity.getContractFields().add(newFieldEntity);
                contractFields.put(newFieldEntity.getField().getFieldName(), newFieldEntity);
            }
        }
        contractRepository.save(contractEntity);

        return contractEntity.getId();
    }

    @PreAuthorize("canReadContract(#contractId)")
    public void deleteContract(long contractId) {
        List<BudgetEntity> budgets = budgetRepository.findByContractId(contractId);
        for (BudgetEntity budgetEntity : budgets) {
            budgetEntity.setContract(null);
        }
        budgetRepository.saveAll(budgets);

        invoiceRepository.deleteInvoiceFieldsByContractId(contractId);
        invoiceRepository.deleteInvoicesByContractId(contractId);

        contractRepository.deleteById(contractId);
    }

    @PreAuthorize("canReadContract(#contractId)")
    public List<Date> getMonthList(long contractId) {
        List<Date> months = new ArrayList<Date>();
        ContractEntity contract = contractRepository.findByIdAndFetchInvoiceFields(contractId);
        Calendar cal = Calendar.getInstance();
        cal.setTime(contract.getStartDate());
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(new Date());
        while (cal.before(currentDate)) {
            months.add(cal.getTime());
            cal.add(Calendar.MONTH, 1);
        }
        return months;
    }

    @PreAuthorize("canReadProject(#projectId)")
    public List<Date> getMonthListForProjectId(long projectId) {
        List<ContractEntity> contracts = contractRepository.findByProjectId(projectId);
        Date startDate = new Date();
        for (ContractEntity contract : contracts) {
            if (contract.getStartDate().before(startDate)) {
                startDate = contract.getStartDate();
            }
        }

        List<Date> months = new ArrayList<Date>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(new Date());
        while (cal.before(currentDate)) {
            months.add(cal.getTime());
            cal.add(Calendar.MONTH, 1);
        }
        return months;
    }

    @PreAuthorize("canReadProject(#projectId)")
    public boolean projectHasContracts(long projectId) {
        List<ContractEntity> contracts = contractRepository.findByProjectId(projectId);
        return (null != contracts && !contracts.isEmpty());
    }

    private ContractFieldEntity createNewContractField(ContractBaseData contractBaseData,
                                                       DynamicAttributeField dynamicAttribute,
                                                       ProjectEntity project) {
        ProjectContractField projectContractField = Optional
                .ofNullable(projectRepository.findContractFieldByName(contractBaseData.getProjectId(), dynamicAttribute.getName().trim()))
                .orElse(new ProjectContractField(0, dynamicAttribute.getName().trim(), project));
        return new ContractFieldEntity(projectContractField, StringUtils.trimToEmpty(dynamicAttribute.getValue()));
    }

    /**
     * Searches for a invoice field in the contract and returns an ContractInvoiceField.
     * Returns the ContractInvoiceField if the contract is present and the field could be found.
     * Else the method returns null!
     *
     * @param contractID Id of the contract to search.
     * @param fieldName  Name of the invoice field.
     * @return ContractInvoiceField or null
     */
    @PreAuthorize("canReadContract(#contractId)")
    public ContractInvoiceField findInvoiceFieldInContractByName(long contractID, String fieldName) {
        Optional<ContractEntity> contractEntityOptional = contractRepository.findById(contractID);
        if (!contractEntityOptional.isPresent()) {
            return null;
        }

        ContractEntity contractEntity = contractEntityOptional.get();
        Optional<ContractInvoiceField> contractInvoiceFieldOptional = contractEntity.getInvoiceFields().stream()
                .filter(contractInvoiceField -> contractInvoiceField.getFieldName().equals(fieldName))
                .findFirst();

        return contractInvoiceFieldOptional.orElse(null);
    }


    /**
     * Searches for a contract and calculates the budget spent by worked minutes and the dailyRate.
     * Returns the BudgetSpent as double if the contract is present and the budget spent could be calculated.
     * Note: The budgetSpent needs to be multiplied by 100, because the previous SQL syntax didn't parsed the money value (2 EUR = 200).
     *
     * @param contractID Id of the contract to search.
     * @return BudgetSpent or 0
     */
    public Double getSpentBudgetByContractId(long contractID) {
        Optional<ContractEntity> contractEntityOptional = contractRepository.findById(contractID);

        if (!contractEntityOptional.isPresent()) {
            return 0.0;
        }

        ContractEntity contractEntity = contractEntityOptional.get();
        Optional<Money> budgetSpentOptional = contractEntity.getBudgets().stream()
                .filter(budgetEntity -> budgetEntity.getContract().getId() == contractID)
                .map(BudgetEntity::getWorkRecords)
                .flatMap(List::stream)
                .map(workRecordEntity ->
                        workRecordEntity.getDailyRate().multipliedBy(workRecordEntity.getMinutes())
                                .dividedBy(60, RoundingMode.CEILING)
                                .dividedBy(8, RoundingMode.CEILING)
                                .multipliedBy(100)
                )
                .reduce(Money::plus);

        return budgetSpentOptional.map(money -> money.getAmount().doubleValue())
                .orElse(0.0);
    }

    /**
     * returns a ContractStatisticBean for a given contract till the given month and year.
     * returns the remaining budget of the contract, the spend budget in budgeteer and the invoiced budget until the given date
     *
     * @param contractId
     * @param month
     * @param year
     * @return
     */
    public ContractStatisticBean getContractStatisticAggregatedByMonthAndYear(long contractId, int month, int year) {
        Optional<ContractEntity> contractEntityOptional = contractRepository.findById(contractId);
        if (!contractEntityOptional.isPresent()) {
            return null;
        }
        ContractEntity contractEntity = contractEntityOptional.get();

        Optional<Money> progressMoneyOptional = calculateProgressMoney(contractEntity, contractId, year, month);
        Optional<Money> invoicedBudgetMoneyOptional = calculateInvoiceBudget(contractEntity, contractId, year, month);

        return new ContractStatisticBean(year,
                progressMoneyOptional.map(money -> money.dividedBy(contractEntity.getBudget().getAmount(), RoundingMode.CEILING).getAmount().doubleValue())
                        .orElse(0.0),
                progressMoneyOptional.map(money -> getScaledMoneyAsLong(contractEntity.getBudget().minus(money)))
                        .orElse(getScaledMoneyAsLong(contractEntity.getBudget())),
                progressMoneyOptional.map(this::getScaledMoneyAsLong).orElse(0L),
                invoicedBudgetMoneyOptional.map(this::getScaledMoneyAsLong).orElse(0L),
                month);
    }

    /**
     * returns a ContractStatisticBean for a given contract till the given month and year.
     * returns the remaining budget of the contract, the spend budget in budgeteer and the invoiced budget until the given date
     *
     * @param contractId
     * @param month
     * @param year
     * @return
     */
    public ContractStatisticBean getContractStatisticByMonthAndYear(long contractId, int month, int year) {
        Optional<ContractEntity> contractEntityOptional = contractRepository.findById(contractId);
        if (!contractEntityOptional.isPresent()) {
            return null;
        }
        ContractEntity contractEntity = contractEntityOptional.get();

        Optional<Money> progressMoneyOptional = calculateProgressMoney(contractEntity, contractId, year, month);

        Optional<Money> remainingContractBudgetOptional = contractEntity.getBudgets().stream()
                .filter(budgetEntity -> budgetEntity.getContract().getId() == contractId)
                .map(BudgetEntity::getWorkRecords)
                .flatMap(List::stream)
                .filter(workRecordEntity -> workRecordEntity.getYear() == year && workRecordEntity.getMonth() == month)
                .map(this::calculateWorkRecordBudget)
                .reduce(Money::plus);

        Optional<Money> invoicedBudgetMoneyOptional = calculateInvoiceBudget(contractEntity, contractId, year, month);

        return new ContractStatisticBean(year,
                contractEntity.getBudget().getAmount().doubleValue() < 10e-16 ? null :
                        progressMoneyOptional.map(money -> money.dividedBy(contractEntity.getBudget().getAmount(), RoundingMode.CEILING).getAmount().doubleValue())
                                .orElse(0.0),
                remainingContractBudgetOptional.map(money -> getScaledMoneyAsLong(contractEntity.getBudget().minus(money)))
                        .orElse(getScaledMoneyAsLong(contractEntity.getBudget())),
                remainingContractBudgetOptional.map(this::getScaledMoneyAsLong).orElse(0L),
                invoicedBudgetMoneyOptional.map(this::getScaledMoneyAsLong).orElse(0L),
                month);
    }

    private Money getScaledMoney(Money money) {
        return money.multipliedBy(100);
    }

    private Long getScaledMoneyAsLong(Money money) {
        return getScaledMoney(money).getAmount().longValue();
    }

    private Optional<Money> calculateProgressMoney(ContractEntity contractEntity, long contractId, int year, int month) {
        return contractEntity.getBudgets().stream()
                .filter(budgetEntity -> budgetEntity.getContract().getId() == contractId)
                .map(BudgetEntity::getWorkRecords)
                .flatMap(List::stream)
                .filter(workRecordEntity -> workRecordEntity.getYear() < year
                        || (workRecordEntity.getYear() == year && workRecordEntity.getMonth() <= month))
                .map(this::calculateWorkRecordBudget)
                .reduce(Money::plus);
    }

    private Optional<Money> calculateInvoiceBudget(ContractEntity contractEntity, long contractId, int year, int month) {
        return contractEntity.getInvoices().stream()
                .filter(invoiceEntity -> invoiceEntity.getContract().getId() == contractId
                        && (invoiceEntity.getYear() < year || (invoiceEntity.getYear() == year && invoiceEntity.getMonth() <= month)))
                .map(InvoiceEntity::getInvoiceSum)
                .reduce(Money::plus);
    }

    private Money calculateWorkRecordBudget(WorkRecordEntity workRecordEntity) {
        return workRecordEntity.getDailyRate()
                .multipliedBy(workRecordEntity.getMinutes())
                .dividedBy(60, RoundingMode.CEILING)
                .dividedBy(8, RoundingMode.CEILING);
    }
}
