package org.wickedsource.budgeteer.service.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordBean;
import org.wickedsource.budgeteer.persistence.record.PlanRecordRepository;
import org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.service.budget.BudgetTagFilter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @InjectMocks
    private RecordService service;
    @Mock
    private WorkRecordRepository workRecordRepository;
    @Mock
    private PlanRecordRepository planRecordRepository;
    @Mock
    private RecordJoiner recordJoiner;

    private static final List<String> EMPTY_STRING_LIST = new ArrayList<>(0);

    @Test
    void testGetWeeklyAggregationForPerson() throws ParseException {
        when(recordJoiner.joinWeekly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenCallRealMethod();
        when(planRecordRepository.aggregateByWeekAndPerson(1L)).thenReturn(createWeeklyAggregatedRecordBeanList());
        when(workRecordRepository.aggregateByWeekAndPerson(1L)).thenReturn(createWeeklyAggregatedRecordBeanList());

        List<AggregatedRecord> resultList = service.getWeeklyAggregationForPerson(1L);

        Assertions.assertThat(resultList)
                .isEqualTo(createWeeklyAggregatedRecordList());
    }

    @Test
    void testGetMonthlyAggregationForPerson() throws ParseException {
        when(recordJoiner.joinMonthly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenCallRealMethod();
        when(planRecordRepository.aggregateByMonthAndPerson(1L)).thenReturn(createMonthlyAggregatedRecordBeanList());
        when(workRecordRepository.aggregateByMonthAndPerson(1L)).thenReturn(createMonthlyAggregatedRecordBeanList());

        List<AggregatedRecord> resultList = service.getMonthlyAggregationForPerson(1L);

        Assertions.assertThat(resultList)
                .isEqualTo(createMonthlyAggregatedRecordList());
    }

    @Test
    void testGetWeeklyAggregationForBudget() throws ParseException {
        when(recordJoiner.joinWeekly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenCallRealMethod();
        when(planRecordRepository.aggregateByWeekAndBudget(1L)).thenReturn(createWeeklyAggregatedRecordBeanList());
        when(workRecordRepository.aggregateByWeekAndBudget(1L)).thenReturn(createWeeklyAggregatedRecordBeanList());

        List<AggregatedRecord> resultList = service.getWeeklyAggregationForBudget(1L);

        Assertions.assertThat(resultList)
                .isEqualTo(createWeeklyAggregatedRecordList());
    }

    @Test
    void testGetMonthlyAggregationForBudget() throws ParseException {
        when(recordJoiner.joinMonthly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenCallRealMethod();
        when(planRecordRepository.aggregateByMonthAndBudget(1L)).thenReturn(createMonthlyAggregatedRecordBeanList());
        when(workRecordRepository.aggregateByMonthAndBudget(1L)).thenReturn(createMonthlyAggregatedRecordBeanList());

        List<AggregatedRecord> resultList = service.getMonthlyAggregationForBudget(1L);

        Assertions.assertThat(resultList)
                .isEqualTo(createMonthlyAggregatedRecordList());
    }

    @Test
    void testGetWeeklyAggregationForBudgets() throws ParseException {
        when(recordJoiner.joinWeekly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenCallRealMethod();
        when(planRecordRepository.aggregateByWeek(1L)).thenReturn(createWeeklyAggregatedRecordBeanList());
        when(workRecordRepository.aggregateByWeek(1L)).thenReturn(createWeeklyAggregatedRecordBeanList());

        List<AggregatedRecord> resultList = service.getWeeklyAggregationForBudgets(new BudgetTagFilter(EMPTY_STRING_LIST, 1L));

        Assertions.assertThat(resultList)
                .isEqualTo(createWeeklyAggregatedRecordList());
    }

    @Test
    void testGetMonthlyAggregationForBudgets() throws ParseException {
        when(recordJoiner.joinMonthly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenCallRealMethod();
        when(planRecordRepository.aggregateByMonth(1L)).thenReturn(createMonthlyAggregatedRecordBeanList());
        when(workRecordRepository.aggregateByMonth(1L)).thenReturn(createMonthlyAggregatedRecordBeanList());

        List<AggregatedRecord> resultList = service.getMonthlyAggregationForBudgets(new BudgetTagFilter(EMPTY_STRING_LIST, 1L));

        Assertions.assertThat(resultList)
                .isEqualTo(createMonthlyAggregatedRecordList());
    }

    private List<AggregatedRecord> createWeeklyAggregatedRecordList() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        return Arrays.asList(
                new AggregatedRecord().setAggregationPeriodTitle("Week 2014-2")
                        .setAggregationPeriodStart(formatter.parse("06.01.2014"))
                        .setAggregationPeriodEnd(formatter.parse("12.01.2014"))
                        .setHours(2d)
                        .setBudgetPlanned_net(MoneyUtil.createMoney(2d))
                        .setBudgetBurned_net(MoneyUtil.createMoney(2d)),
                new AggregatedRecord().setAggregationPeriodTitle("Week 2014-3")
                        .setAggregationPeriodStart(formatter.parse("13.01.2014"))
                        .setAggregationPeriodEnd(formatter.parse("19.01.2014"))
                        .setHours(3d)
                        .setBudgetPlanned_net(MoneyUtil.createMoney(4d))
                        .setBudgetBurned_net(MoneyUtil.createMoney(4d)),
                new AggregatedRecord().setAggregationPeriodTitle("Week 2014-4")
                        .setAggregationPeriodStart(formatter.parse("20.01.2014"))
                        .setAggregationPeriodEnd(formatter.parse("26.01.2014"))
                        .setHours(4d)
                        .setBudgetPlanned_net(MoneyUtil.createMoney(6d))
                        .setBudgetBurned_net(MoneyUtil.createMoney(6d)));
    }

    private List<AggregatedRecord> createMonthlyAggregatedRecordList() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        return Arrays.asList(
                new AggregatedRecord().setAggregationPeriodTitle("Month 2014-03")
                        .setAggregationPeriodStart(formatter.parse("01.03.2014"))
                        .setAggregationPeriodEnd(formatter.parse("31.03.2014"))
                        .setHours(2d)
                        .setBudgetPlanned_net(MoneyUtil.createMoney(2d))
                        .setBudgetBurned_net(MoneyUtil.createMoney(2d)),
                new AggregatedRecord().setAggregationPeriodTitle("Month 2014-04")
                        .setAggregationPeriodStart(formatter.parse("01.04.2014"))
                        .setAggregationPeriodEnd(formatter.parse("30.04.2014"))
                        .setHours(3d)
                        .setBudgetPlanned_net(MoneyUtil.createMoney(4d))
                        .setBudgetBurned_net(MoneyUtil.createMoney(4d)),
                new AggregatedRecord().setAggregationPeriodTitle("Month 2014-05")
                        .setAggregationPeriodStart(formatter.parse("01.05.2014"))
                        .setAggregationPeriodEnd(formatter.parse("31.05.2014"))
                        .setHours(4d)
                        .setBudgetPlanned_net(MoneyUtil.createMoney(6d))
                        .setBudgetBurned_net(MoneyUtil.createMoney(6d)));
    }

    private List<WeeklyAggregatedRecordBean> createWeeklyAggregatedRecordBeanList() {
        return Arrays.asList(
                new WeeklyAggregatedRecordBean(2014, 2, 2d, 200L),
                new WeeklyAggregatedRecordBean(2014, 3, 3d, 400L),
                new WeeklyAggregatedRecordBean(2014, 4, 4d, 600L));
    }

    private List<MonthlyAggregatedRecordBean> createMonthlyAggregatedRecordBeanList() {
        return Arrays.asList(
                new MonthlyAggregatedRecordBean(2014, 2, 2d, 200L),
                new MonthlyAggregatedRecordBean(2014, 3, 3d, 400L),
                new MonthlyAggregatedRecordBean(2014, 4, 4d, 600L));
    }
}
