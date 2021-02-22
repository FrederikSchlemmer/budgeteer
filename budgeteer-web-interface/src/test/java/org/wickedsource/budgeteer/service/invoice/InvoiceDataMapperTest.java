package org.wickedsource.budgeteer.service.invoice;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractInvoiceField;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceEntity;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceFieldEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.service.contract.DynamicAttributeField;
import org.wickedsource.budgeteer.web.components.fileUpload.FileUploadModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

class InvoiceDataMapperTest {

    /**
     * Tests whether different invoiced associated with different contracts will get the same dynamic attributes
     */
    @Test
    void testMapWithDifferentProjectsButSameAttributes() {
        InvoiceDataMapper mapper = new InvoiceDataMapper();

        List<InvoiceEntity> invoiceList = new LinkedList<InvoiceEntity>();
        invoiceList.add(getDummyInvoiceEntity());
        invoiceList.add(getDummyInvoiceEntity2());

        List<InvoiceBaseData> mappedData = mapper.map(invoiceList, true);

        Assertions.assertThat(mappedData)
                .hasSize(2)
                .containsExactly(new InvoiceBaseData()
                                .setInvoiceId(1)
                                .setContractId(1)
                                .setContractName("Contract 1")
                                .setInvoiceName("InvoiceEntity 1")
                                .setSum(MoneyUtil.createMoney(200))
                                .setSum_gross(MoneyUtil.createMoney(220))
                                .setTaxAmount(MoneyUtil.createMoney(20))
                                .setTaxRate(BigDecimal.valueOf(10))
                                .setInternalNumber("InvoiceEntity 1")
                                .setYear(2015)
                                .setMonth(3)
                                .setPaidDate(Date.from(Instant.EPOCH))
                                .setDueDate(null)
                                .setFileUploadModel(new FileUploadModel()
                                        .setFileName("FileName1")
                                        .setFile(null)
                                        .setChanged(false)
                                        .setLink("http://Link1"))
                                .setDynamicInvoiceFields(Arrays.asList(
                                        new DynamicAttributeField("contractInvoiceField1 Name", "contractInvoiceField1 Value"),
                                        new DynamicAttributeField("contractInvoiceField2 Name", "contractInvoiceField2 Value"),
                                        new DynamicAttributeField("contractInvoiceField3 Name", "")
                                )),
                        new InvoiceBaseData()
                                .setInvoiceId(2)
                                .setContractId(2)
                                .setContractName("Contract 2")
                                .setInvoiceName("InvoiceEntity 2")
                                .setSum(MoneyUtil.createMoney(200))
                                .setSum_gross(MoneyUtil.createMoney(240))
                                .setTaxAmount(MoneyUtil.createMoney(40))
                                .setTaxRate(BigDecimal.valueOf(20))
                                .setInternalNumber("InvoiceEntity 2")
                                .setYear(2015)
                                .setMonth(3)
                                .setPaidDate(Date.from(Instant.EPOCH))
                                .setDueDate(null)
                                .setFileUploadModel(new FileUploadModel()
                                        .setFileName(null)
                                        .setFile(null)
                                        .setChanged(false)
                                        .setLink(null))
                                .setDynamicInvoiceFields(Arrays.asList(
                                        new DynamicAttributeField("contractInvoiceField1 Name", ""),
                                        new DynamicAttributeField("contractInvoiceField2 Name", ""),
                                        new DynamicAttributeField("contractInvoiceField3 Name", "contractInvoiceField3 Value")
                                ))
                );
    }


    @Test
    void testMap() {
        InvoiceDataMapper mapper = new InvoiceDataMapper();

        InvoiceBaseData mappedElement = mapper.map(getDummyInvoiceEntity());

        Assertions.assertThat(mappedElement)
                .isEqualTo(new InvoiceBaseData()
                        .setInvoiceId(1)
                        .setInvoiceName("InvoiceEntity 1")
                        .setSum(MoneyUtil.createMoneyFromCents(20000))
                        .setSum_gross(MoneyUtil.createMoneyFromCents(22000))
                        .setTaxAmount(MoneyUtil.createMoneyFromCents(2000))
                        .setTaxRate(BigDecimal.valueOf(10))
                        .setInternalNumber("InvoiceEntity 1")
                        .setYear(2015)
                        .setMonth(3)
                        .setPaidDate(Date.from(Instant.EPOCH))
                        .setContractName("Contract 1")
                        .setContractId(1)
                        .setFileUploadModel(new FileUploadModel()
                                .setFileName("FileName1")
                                .setFile(null)
                                .setChanged(false)
                                .setLink("http://Link1"))
                        .setDynamicInvoiceFields(Arrays.asList(
                                new DynamicAttributeField("contractInvoiceField1 Name", "contractInvoiceField1 Value"),
                                new DynamicAttributeField("contractInvoiceField2 Name", "contractInvoiceField2 Value")
                        ))
                );
    }


    private InvoiceEntity getDummyInvoiceEntity() {
        ProjectEntity project1 = new ProjectEntity();
        project1.setId(1);
        project1.setName("Project1");

        /*
         * Set up a contract
         */
        ContractEntity contract1 = new ContractEntity();
        contract1.setId(1);
        contract1.setProject(project1);
        contract1.setName("Contract 1");
        contract1.setInvoiceFields(new HashSet<ContractInvoiceField>());
        contract1.setTaxRate(new BigDecimal(10));

        /*
         * Add zwo ContractInvoiceFields to the contract
         */
        ContractInvoiceField contractInvoiceField1 = new ContractInvoiceField();
        contractInvoiceField1.setId(1);
        contractInvoiceField1.setContract(contract1);
        contractInvoiceField1.setFieldName("contractInvoiceField1 Name");
        contract1.getInvoiceFields().add(contractInvoiceField1);

        ContractInvoiceField contractInvoiceField2 = new ContractInvoiceField();
        contractInvoiceField2.setId(2);
        contractInvoiceField2.setContract(contract1);
        contractInvoiceField2.setFieldName("contractInvoiceField2 Name");
        contract1.getInvoiceFields().add(contractInvoiceField2);

        /*
         * Create the first Invoice
         */
        InvoiceEntity invoiceEntity = new InvoiceEntity();
        invoiceEntity.setId(1);
        invoiceEntity.setName("InvoiceEntity 1");
        invoiceEntity.setInvoiceSum(MoneyUtil.createMoneyFromCents(20000));
        invoiceEntity.setInternalNumber("InvoiceEntity 1");
        invoiceEntity.setYear(2015);
        invoiceEntity.setMonth(3);
        invoiceEntity.setPaidDate(Date.from(Instant.EPOCH));

        invoiceEntity.setFileName("FileName1");
        invoiceEntity.setFile(null);
        invoiceEntity.setLink("Link1");

        invoiceEntity.setContract(contract1);
        invoiceEntity.setDynamicFields(new LinkedList<InvoiceFieldEntity>());
        /*
         * Add some Dynamic Invoice Fields
         */
        InvoiceFieldEntity dynamicField1 = new InvoiceFieldEntity();
        dynamicField1.setId(1);
        dynamicField1.setField(contractInvoiceField1);
        dynamicField1.setValue("contractInvoiceField1 Value");
        invoiceEntity.getDynamicFields().add(dynamicField1);

        InvoiceFieldEntity dynamicField2 = new InvoiceFieldEntity();
        dynamicField2.setId(2);
        dynamicField2.setField(contractInvoiceField2);
        dynamicField2.setValue("contractInvoiceField2 Value");
        invoiceEntity.getDynamicFields().add(dynamicField2);

        return invoiceEntity;
    }

    private InvoiceEntity getDummyInvoiceEntity2() {
        ProjectEntity project1 = new ProjectEntity();
        project1.setId(1);
        project1.setName("Project1");

        ContractEntity contract2 = new ContractEntity();
        contract2.setId(2);
        contract2.setProject(project1);
        contract2.setName("Contract 2");
        contract2.setInvoiceFields(new HashSet<ContractInvoiceField>());
        contract2.setTaxRate(BigDecimal.valueOf(20));

        ContractInvoiceField contractInvoiceField1 = new ContractInvoiceField();
        contractInvoiceField1.setId(3);
        contractInvoiceField1.setContract(contract2);
        contractInvoiceField1.setFieldName("contractInvoiceField3 Name");
        contract2.getInvoiceFields().add(contractInvoiceField1);

        InvoiceEntity invoiceEntity = new InvoiceEntity();
        invoiceEntity.setId(2);
        invoiceEntity.setName("InvoiceEntity 2");
        invoiceEntity.setInvoiceSum(MoneyUtil.createMoneyFromCents(20000));
        invoiceEntity.setInternalNumber("InvoiceEntity 2");
        invoiceEntity.setYear(2015);
        invoiceEntity.setMonth(3);
        invoiceEntity.setPaidDate(Date.from(Instant.EPOCH));
        invoiceEntity.setContract(contract2);
        invoiceEntity.setDynamicFields(new LinkedList<InvoiceFieldEntity>());

        InvoiceFieldEntity dynamicField1 = new InvoiceFieldEntity();
        dynamicField1.setId(3);
        dynamicField1.setField(contractInvoiceField1);
        dynamicField1.setValue("contractInvoiceField3 Value");
        invoiceEntity.getDynamicFields().add(dynamicField1);

        return invoiceEntity;
    }
}
