package org.wickedsource.budgeteer.service.contract;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.IntegrationTestTemplate;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractDataMapperTest {

    @InjectMocks
    private ContractDataMapper contractDataMapper;
    @Mock
    private ContractRepository contractRepository;

    @Test
    void whenTaxrateIsNotNull() {
        ContractEntity contractEntity = new ContractEntity()
                .setId(4)
                .setName("Test")
                .setTaxRate(BigDecimal.valueOf(100.0000))
                .setProject(new ProjectEntity()
                        .setId(4)
                        .setName("project4")
                        .setAuthorizedUsers(Collections.emptyList())
                        .setContractFields(Collections.emptySet())
                        .setProjectStart(null)
                        .setProjectEnd(null))
                .setBudget(MoneyUtil.createMoney(0.01))
                .setInternalNumber("Test")
                .setType(ContractEntity.ContractType.T_UND_M)
                .setLink(null)
                .setFileName(null)
                .setInvoices(Collections.emptyList())
                .setInvoiceFields(Collections.emptySet());

        when(contractRepository.getBudgetLeftByContractId(4L)).thenReturn(0.01);
        when(contractRepository.getSpentBudgetByContractId(4L)).thenReturn(0.00);

        ContractBaseData contractBaseData = contractDataMapper.map(contractEntity);

        Assertions.assertThat(contractBaseData.getTaxRate())
                .isCloseTo(BigDecimal.valueOf(100), Percentage.withPercentage(10e-8));
    }

}
