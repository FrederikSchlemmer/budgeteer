package org.wickedsource.budgeteer.service.contract.report;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.contract.ContractStatisticBean;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractReportMonthlyDataMapperTest {

    @InjectMocks
    private ContractReportMonthlyDataMapper testSubject;
    @Mock
    private ContractRepository contractRepository;

    @Test
    void whenTaxrateIsNull() {
        ContractEntity contractEntity = new ContractEntity()
                .setId(3)
                .setName("Test")
                .setProject(new ProjectEntity()
                        .setId(3)
                        .setName("project3")
                        .setAuthorizedUsers(Collections.emptyList())
                        .setProjectStart(null)
                        .setProjectEnd(null))
                .setBudget(MoneyUtil.createMoney(1))
                .setInternalNumber("Test")
                .setType(ContractEntity.ContractType.FIXED_PRICE)
                .setLink(null)
                .setFileName(null);
        ContractStatisticBean contractStatisticBean = new ContractStatisticBean(2021,
                0.0, 100, 0, 0, 0);

        when(contractRepository.getContractStatisticByMonthAndYear(eq(3L), any(), any()))
                .thenReturn(contractStatisticBean);

        ContractReportData contractBaseData = testSubject.map(contractEntity, new Date());

        Assertions.assertThat(contractBaseData.getTaxRate())
                .isCloseTo(0.00, Percentage.withPercentage(10e-8));
    }

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
                        .setProjectStart(null)
                        .setProjectEnd(null))
                .setBudget(MoneyUtil.createMoney(0.01))
                .setInternalNumber("Test")
                .setType(ContractEntity.ContractType.T_UND_M)
                .setLink(null)
                .setFileName(null);
        ContractStatisticBean contractStatisticBean = new ContractStatisticBean(2021,
                0.0, 1, 0, 0, 0);

        when(contractRepository.getContractStatisticByMonthAndYear(eq(4L), any(), any()))
                .thenReturn(contractStatisticBean);

        ContractReportData contractBaseData = testSubject.map(contractEntity, new Date());

        Assertions.assertThat(contractBaseData.getTaxRate())
                .isCloseTo(1.00, Percentage.withPercentage(10e-8));
    }
}
