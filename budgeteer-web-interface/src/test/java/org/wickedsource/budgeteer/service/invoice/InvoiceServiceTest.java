package org.wickedsource.budgeteer.service.invoice;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractInvoiceField;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceEntity;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceFieldEntity;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.service.contract.ContractService;
import org.wickedsource.budgeteer.service.contract.DynamicAttributeField;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @InjectMocks
    private InvoiceService invoiceService;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private ContractService contractService;

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

    /**
     * Save a new Invoice associated with a Contract that does not have any ContractInvoiceFields
     */
    @SneakyThrows
    @Test
    void testSaveNewInvoice() {
        ContractEntity contractEntity = new ContractEntity()
                .setId(1L)
                .setName("Test")
                .setProject(new ProjectEntity()
                        .setId(1L)
                        .setName("project1")
                        .setAuthorizedUsers(Collections.emptyList())
                        .setProjectEnd(null)
                        .setProjectStart(null))
                .setContractFields(Collections.emptyList())
                .setBudget(MoneyUtil.createMoney(1))
                .setTaxRate(BigDecimal.valueOf(10))
                .setInternalNumber("Test")
                .setStartDate(formatter.parse("01.01.2015"))
                .setType(ContractEntity.ContractType.FIXED_PRICE)
                .setBudgets(Collections.emptyList())
                .setInvoices(Collections.emptyList())
                .setInvoiceFields(Collections.emptySet());
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contractEntity));

        InvoiceEntity invoiceEntity = new InvoiceEntity()
                .setId(0L)
                .setName("Invoice Name")
                .setInvoiceSum(MoneyUtil.createMoney(2000))
                .setInternalNumber("Internal Number")
                .setYear(2015)
                .setMonth(2)
                .setDate(formatter.parse("01.02.2015"))
                .setContract(contractEntity)
                .setDynamicFields(Arrays.asList(
                        new InvoiceFieldEntity()
                                .setId(0L)
                                .setValue("test0")
                                .setField(new ContractInvoiceField(0L, "test0", contractEntity)),
                        new InvoiceFieldEntity()
                                .setId(0L)
                                .setValue("test1")
                                .setField(new ContractInvoiceField(0L, "test1", contractEntity)),
                        new InvoiceFieldEntity()
                                .setId(0L)
                                .setValue("test2")
                                .setField(new ContractInvoiceField(0L, "test2", contractEntity)),
                        new InvoiceFieldEntity()
                                .setId(0L)
                                .setValue("test3")
                                .setField(new ContractInvoiceField(0L, "test3", contractEntity)),
                        new InvoiceFieldEntity()
                                .setId(0L)
                                .setValue("test4")
                                .setField(new ContractInvoiceField(0L, "test4", contractEntity))
                ));

        InvoiceBaseData testObject = getDummyInvoice();
        testObject.setContractId(1);

        long newContractId = invoiceService.save(testObject);

        Assertions.assertThat(newContractId)
                .isZero();
        verify(invoiceRepository, times(1)).save(invoiceEntity);
    }

    /**
     * Save a new Invoice associated with a Contract that has two ContractInvoiceFields
     */
    @SneakyThrows
    @Test
    void testSaveNewInvoice2() {
        ContractEntity contractEntity = new ContractEntity()
                .setId(2L)
                .setName("Test")
                .setProject(new ProjectEntity()
                        .setId(1L)
                        .setName("project1")
                        .setAuthorizedUsers(Collections.emptyList())
                        .setProjectEnd(null)
                        .setProjectStart(null))
                .setContractFields(Collections.emptyList())
                .setBudget(MoneyUtil.createMoney(1))
                .setTaxRate(BigDecimal.valueOf(10))
                .setInternalNumber("Test")
                .setStartDate(formatter.parse("01.01.2015"))
                .setType(ContractEntity.ContractType.FIXED_PRICE)
                .setBudgets(Collections.emptyList())
                .setInvoices(Collections.emptyList());
        contractEntity.setInvoiceFields(new HashSet<>(Arrays.asList(
                        new ContractInvoiceField(400L, "Test Contract Field 2", contractEntity),
                new ContractInvoiceField(300L, "Test Contract Field", contractEntity))));
        when(contractRepository.findById(2L)).thenReturn(Optional.of(contractEntity));

        InvoiceEntity invoiceEntity = new InvoiceEntity()
                .setId(0L)
                .setName("Invoice Name")
                .setInvoiceSum(MoneyUtil.createMoney(2000))
                .setInternalNumber("Internal Number")
                .setYear(2015)
                .setMonth(2)
                .setDate(formatter.parse("01.02.2015"))
                .setContract(contractEntity)
                .setDynamicFields(Arrays.asList(
                        new InvoiceFieldEntity()
                                .setId(0L)
                                .setValue("test0")
                                .setField(new ContractInvoiceField(0L, "test0", contractEntity)),
                        new InvoiceFieldEntity()
                                .setId(0L)
                                .setValue("test1")
                                .setField(new ContractInvoiceField(0L, "test1", contractEntity)),
                        new InvoiceFieldEntity()
                                .setId(0L)
                                .setValue("test2")
                                .setField(new ContractInvoiceField(0L, "test2", contractEntity)),
                        new InvoiceFieldEntity()
                                .setId(0L)
                                .setValue("test3")
                                .setField(new ContractInvoiceField(0L, "test3", contractEntity)),
                        new InvoiceFieldEntity()
                                .setId(0L)
                                .setValue("test4")
                                .setField(new ContractInvoiceField(0L, "test4", contractEntity))
                ));

        InvoiceBaseData testObject = getDummyInvoice();
        testObject.setContractId(2);

        long newContractId = invoiceService.save(testObject);

        Assertions.assertThat(newContractId)
                .isZero();
        verify(invoiceRepository, times(1)).save(invoiceEntity);
    }

    private InvoiceBaseData getDummyInvoice() {
        InvoiceBaseData result = new InvoiceBaseData();
        result.setInvoiceId(0);
        result.setPaidDate(null);
        result.setContractId(1);
        result.setContractName("Test");
        result.setInternalNumber("Internal Number");
        result.setInvoiceName("Invoice Name");
        result.setMonth(2);
        result.setSum(MoneyUtil.createMoney(2000));
        result.setYear(2015);
        result.setDynamicInvoiceFields(getDummyDynamicInvoiceFields());
        return result;
    }

    private List<DynamicAttributeField> getDummyDynamicInvoiceFields() {
        List<DynamicAttributeField> result = new LinkedList<DynamicAttributeField>();
        DynamicAttributeField data = new DynamicAttributeField();
        for (int i = 0; i < 5; i++) {
            data = new DynamicAttributeField();
            data.setName("test" + i);
            data.setValue("test" + i);
            result.add(data);
        }
        return result;
    }
}
