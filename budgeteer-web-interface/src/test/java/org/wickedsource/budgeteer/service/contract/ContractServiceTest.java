package org.wickedsource.budgeteer.service.contract;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractFieldEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectContractField;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @InjectMocks
    private ContractService contractService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ContractRepository contractRepository;

    /**
     * Save a new Contract associated with a Project that does not have any ProjectContractFields
     */
    @Test
    void testSaveNewContract() {
        ContractBaseData testObject = new ContractBaseData()
                .setBudget(MoneyUtil.createMoney(12))
                .setContractId(0)
                .setProjectId(100)
                .setContractName("Test Contract")
                .setContractAttributes(getListOfContractFields())
                .setTaxRate(BigDecimal.ZERO);
        testObject.getContractAttributes().add(new DynamicAttributeField("test4", "test4"));

        ContractEntity contractEntity = new ContractEntity()
                .setId(0)
                .setName("Test Contract")
                .setProject(createProjectEntity(100L, "project1"))
                .setBudget(MoneyUtil.createMoney(12))
                .setTaxRate(BigDecimal.ZERO)
                .setContractFields(Arrays.asList(
                        new ContractFieldEntity()
                                .setId(0)
                                .setValue("test0")
                                .setContract(null)
                                .setField(new ProjectContractField()
                                        .setId(0)
                                        .setFieldName("test0")
                                        .setProject(createProjectEntity(100L, "project1"))),
                        new ContractFieldEntity()
                                .setId(0)
                                .setValue("test1")
                                .setContract(null)
                                .setField(new ProjectContractField()
                                        .setId(0)
                                        .setFieldName("test1")
                                        .setProject(createProjectEntity(100L, "project1"))),
                        new ContractFieldEntity()
                                .setId(0)
                                .setValue("test2")
                                .setContract(null)
                                .setField(new ProjectContractField()
                                        .setId(0)
                                        .setFieldName("test2")
                                        .setProject(createProjectEntity(100L, "project1"))),
                        new ContractFieldEntity()
                                .setId(0)
                                .setValue("test3")
                                .setContract(null)
                                .setField(new ProjectContractField()
                                        .setId(0)
                                        .setFieldName("test3")
                                        .setProject(createProjectEntity(100L, "project1"))),
                        new ContractFieldEntity()
                                .setId(0)
                                .setValue("test4")
                                .setContract(null)
                                .setField(new ProjectContractField()
                                        .setId(0)
                                        .setFieldName("test4")
                                        .setProject(createProjectEntity(100L, "project1")))
                ));

        when(projectRepository.findById(100L)).thenReturn(Optional.of(createProjectEntity(100L, "project1")));

        long newContractId = contractService.save(testObject);

        verify(contractRepository, times(1)).save(contractEntity);
        Assertions.assertThat(newContractId)
                .isEqualTo(0L);
    }

    /**
     * Save a new Contract associated with a Project that already has some ProjectContractFields
     */
    @Test
    void testSaveNewContract2() {
        ContractBaseData contractBaseData = new ContractBaseData()
                .setContractId(0)
                .setProjectId(200)
                .setContractName("Test Contract")
                .setContractAttributes(getListOfContractFields())
                .setTaxRate(BigDecimal.ZERO);
        contractBaseData.getContractAttributes()
                .add(new DynamicAttributeField("test4", "test4"));

        ProjectEntity projectEntity = createProjectEntity(200L, "project2");
        projectEntity.getContractFields()
                .addAll(Arrays.asList(
                        new ProjectContractField().setId(100).setFieldName("test0").setProject(createProjectEntity(200L, "project2")),
                        new ProjectContractField().setId(200).setFieldName("test1").setProject(createProjectEntity(200L, "project2"))));

        ContractEntity contractEntity = new ContractEntity()
                .setId(0)
                .setName("Test Contract")
                .setProject(projectEntity)
                .setBudget(null)
                .setTaxRate(BigDecimal.ZERO)
                .setContractFields(Arrays.asList(
                        new ContractFieldEntity()
                                .setId(0)
                                .setValue("test0")
                                .setContract(null)
                                .setField(new ProjectContractField()
                                        .setId(0)
                                        .setFieldName("test0")
                                        .setProject(projectEntity)),
                        new ContractFieldEntity()
                                .setId(0)
                                .setValue("test1")
                                .setContract(null)
                                .setField(new ProjectContractField()
                                        .setId(0)
                                        .setFieldName("test1")
                                        .setProject(projectEntity)),
                        new ContractFieldEntity()
                                .setId(0)
                                .setValue("test2")
                                .setContract(null)
                                .setField(new ProjectContractField()
                                        .setId(0)
                                        .setFieldName("test2")
                                        .setProject(projectEntity)),
                        new ContractFieldEntity()
                                .setId(0)
                                .setValue("test3")
                                .setContract(null)
                                .setField(new ProjectContractField()
                                        .setId(0)
                                        .setFieldName("test3")
                                        .setProject(projectEntity)),
                        new ContractFieldEntity()
                                .setId(0)
                                .setValue("test4")
                                .setContract(null)
                                .setField(new ProjectContractField()
                                        .setId(0)
                                        .setFieldName("test4")
                                        .setProject(projectEntity))
                ));

        when(projectRepository.findById(200L)).thenReturn(Optional.of(projectEntity));

        long newContractId = contractService.save(contractBaseData);

        verify(contractRepository, times(1)).save(contractEntity);
        Assertions.assertThat(newContractId)
                .isEqualTo(0L);
    }

    @Test
    void testGetEmptyContractModel() {
        when(projectRepository.findById(100L)).thenReturn(Optional.of(createProjectEntity(100L, "project1")));

        ProjectEntity projectEntity = createProjectEntity(300L, "project2");
        projectEntity.getContractFields().addAll(
                Arrays.asList(
                        new ProjectContractField(0, "test0", projectEntity),
                        new ProjectContractField(1, "test1", projectEntity))
        );
        when(projectRepository.findById(300L)).thenReturn(Optional.of(projectEntity));

        ContractBaseData baseData = contractService.getEmptyContractModel(100L);

        Assertions.assertThat(baseData.getContractAttributes())
                .isEmpty();

        baseData = contractService.getEmptyContractModel(300L);

        Assertions.assertThat(baseData.getContractAttributes())
                .hasSize(2)
                .extracting(DynamicAttributeField::getName)
                .containsAll(Arrays.asList("test0", "test1"));
    }

    private ProjectEntity createProjectEntity(Long id, String projectName) {
        return new ProjectEntity()
                .setId(id)
                .setName(projectName)
                .setAuthorizedUsers(Collections.emptyList())
                .setProjectStart(null)
                .setProjectEnd(null)
                .setContractFields(new HashSet<>());
    }

    private List<DynamicAttributeField> getListOfContractFields() {
        List<DynamicAttributeField> result = new LinkedList<DynamicAttributeField>();
        for (int i = 0; i < 5; i++) {
            DynamicAttributeField data = new DynamicAttributeField();
            data.setName("test" + i);
            data.setValue("test" + i);
            result.add(data);
        }
        return result;
    }
}
