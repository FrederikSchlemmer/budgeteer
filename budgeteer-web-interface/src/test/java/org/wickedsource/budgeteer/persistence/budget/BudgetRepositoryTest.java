package org.wickedsource.budgeteer.persistence.budget;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wickedsource.budgeteer.IntegrationTestTemplate;
import org.wickedsource.budgeteer.service.budget.BudgetService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

class BudgetRepositoryTest extends IntegrationTestTemplate {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetService budgetService;

//    @Test
//    @DatabaseSetup("findByAtLeastOneTag.xml")
//    @DatabaseTearDown(value = "findByAtLeastOneTag.xml", type = DatabaseOperation.DELETE_ALL)
//    void testFindByAtLeastOneTag() {
//        Collection<BudgetEntity> budgets = new TreeSet<>(budgetService.findBudgetWithAtLeastOneTag(1L, Arrays.asList("Tag 1", "Tag 3")));
//        Assertions.assertEquals(2, budgets.size());
//        Collection<BudgetEntity> budgets2 = new TreeSet<>(budgetService.findBudgetWithAtLeastOneTag(1L, Arrays.asList("Tag 3")));
//        Assertions.assertEquals(1, budgets2.size());
//    }

//    @Test
//    @DatabaseSetup("getMissingBudgetTotals.xml")
//    @DatabaseTearDown(value = "getMissingBudgetTotals.xml", type = DatabaseOperation.DELETE_ALL)
//    void testGetMissingBudgetTotalsForProject() {
//        List<MissingBudgetTotalBean> missingTotals = budgetService.findBudgetWithMissingTotal(1L);
//        Assertions.assertEquals(1, missingTotals.size());
//        Assertions.assertEquals(1L, missingTotals.get(0).getBudgetId());
//        Assertions.assertEquals("Budget 1", missingTotals.get(0).getBudgetName());
//    }

//    @Test
//    @DatabaseSetup("getMissingBudgetTotals.xml")
//    @DatabaseTearDown(value = "getMissingBudgetTotals.xml", type = DatabaseOperation.DELETE_ALL)
//    void testGetMissingBudgetTotalForBudget() {
//        MissingBudgetTotalBean missingTotal = budgetService.findBudgetByBudgetIdWithMissingTotal(1L);
//        Assertions.assertEquals(1L, missingTotal.getBudgetId());
//        Assertions.assertEquals("Budget 1", missingTotal.getBudgetName());
//        Assertions.assertNull(budgetService.findBudgetByBudgetIdWithMissingTotal(2L));
//    }

    @Test
    @DatabaseSetup("getTaxCoefficient.xml")
    @DatabaseTearDown(value = "getTaxCoefficient.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetTaxCoefficientForBudget() {
        Double taxCoefficient1 = budgetService.findTaxCoefficientByBudget(1L);
        Double taxCoefficient2 = budgetService.findTaxCoefficientByBudget(2L);
        Double taxCoefficient3 = budgetService.findTaxCoefficientByBudget(3L);
        Double taxCoefficient4 = budgetService.findTaxCoefficientByBudget(4L);
        Assertions.assertEquals(2.0, taxCoefficient1, 10e-8);
        Assertions.assertEquals(1.0, taxCoefficient2, 10e-8);
        Assertions.assertEquals(1.19, taxCoefficient3, 10e-8);
        Assertions.assertEquals(1.0, taxCoefficient4, 10e-8);
    }

}
