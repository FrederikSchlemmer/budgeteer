package org.wickedsource.budgeteer.service.statistics;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceRepository;
import org.wickedsource.budgeteer.persistence.record.*;
import org.wickedsource.budgeteer.service.DateProvider;
import org.wickedsource.budgeteer.service.DateUtil;
import org.wickedsource.budgeteer.service.budget.BudgetTagFilter;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @InjectMocks
    private StatisticsService service;
    @Mock
    private WorkRecordRepository workRecordRepository;
    @Mock
    private PlanRecordRepository planRecordRepository;
    @Mock
    private DateUtil dateUtil;
    @Mock
    private ShareBeanToShareMapper shareBeanToShareMapper;

    private final DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    private static final Comparator<MoneySeries> moneySeriesComparator = Comparator.comparing(MoneySeries::getName);

    @Test
    void testGetWeeklyBudgetBurnedForProject() throws ParseException {
        when(dateUtil.weeksAgo(5)).thenReturn(getWeeksAgoDate(format.parse("29.01.2015"), 5));
        when(workRecordRepository.aggregateByWeekForProject(anyLong(), any(Date.class))).thenReturn(createLast5Weeks());

        List<Money> resultList = service.getWeeklyBudgetBurnedForProject(1L, 5);

        Assertions.assertThat(resultList)
                .hasSize(5)
                .containsAll(Arrays.asList(MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L)));
    }

    @Test
    void testGetWeeklyBudgetPlannedForProject() throws ParseException {
        when(dateUtil.weeksAgo(5)).thenReturn(getWeeksAgoDate(format.parse("29.01.2015"), 5));
        when(planRecordRepository.aggregateByWeekForProject(anyLong(), any(Date.class))).thenReturn(createLast5Weeks());

        List<Money> resultList = service.getWeeklyBudgetPlannedForProject(1L, 5);

        Assertions.assertThat(resultList)
                .hasSize(5)
                .containsAll(Arrays.asList(MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L)));
    }

    @Test
    void testGetWeeklyBudgetBurnedForPerson() throws Exception {
        when(dateUtil.weeksAgo(5)).thenReturn(getWeeksAgoDate(format.parse("29.01.2015"), 5));
        when(workRecordRepository.aggregateByWeekForPerson(anyLong(), any(Date.class))).thenReturn(createLast5Weeks());

        List<Money> resultList = service.getWeeklyBudgetBurnedForPerson(1L, 5);

        Assertions.assertThat(resultList)
                .hasSize(5)
                .containsAll(Arrays.asList(MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L)));
    }

    @Test
    void testGetWeeklyBudgetPlannedForPerson() throws Exception {
        when(dateUtil.weeksAgo(5)).thenReturn(getWeeksAgoDate(format.parse("29.01.2015"), 5));
        when(planRecordRepository.aggregateByWeekForPerson(anyLong(), any(Date.class))).thenReturn(createLast5Weeks());

        List<Money> resultList = service.getWeeklyBudgetPlannedForPerson(1L, 5);

        Assertions.assertThat(resultList)
                .hasSize(5)
                .containsAll(Arrays.asList(MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L)));
    }

    @Test
    void testGetAvgDailyRateForPreviousDays() throws Exception {
        when(dateUtil.daysAgo(5)).thenReturn(getDaysAgoDate(format.parse("05.01.2015"), 5));
        when(workRecordRepository.getAverageDailyRatesPerDay(anyLong(), any(Date.class))).thenReturn(createLast5Days());

        List<Money> resultList = service.getAvgDailyRateForPreviousDays(1L, 5);

        Assertions.assertThat(resultList)
                .hasSize(5)
                .containsAll(Arrays.asList(MoneyUtil.createMoneyFromCents(100L),
                        MoneyUtil.createMoneyFromCents(200L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400L),
                        MoneyUtil.createMoneyFromCents(500L)));
    }

    @Test
    void testGetBudgetDistribution() {
        when(shareBeanToShareMapper.map(ArgumentMatchers.<ShareBean>anyList())).thenCallRealMethod();
        when(shareBeanToShareMapper.map(any(ShareBean.class))).thenCallRealMethod();
        when(workRecordRepository.getBudgetShareForPerson(1L)).thenReturn(createShares());

        List<Share> shares = service.getBudgetDistribution(1L);

        Assertions.assertThat(shares)
                .hasSize(4)
                .extracting(Share::getName, Share::getShare)
                .containsAll(Arrays.asList(
                        Tuple.tuple("share1", MoneyUtil.createMoneyFromCents(10000L)),
                        Tuple.tuple("share2", MoneyUtil.createMoneyFromCents(20000L)),
                        Tuple.tuple("share3", MoneyUtil.createMoneyFromCents(30000L)),
                        Tuple.tuple("share4", MoneyUtil.createMoneyFromCents(40000L))));
    }

    @Test
    void testGetPeopleDistribution() {
        when(shareBeanToShareMapper.map(ArgumentMatchers.<ShareBean>anyList())).thenCallRealMethod();
        when(shareBeanToShareMapper.map(any(ShareBean.class))).thenCallRealMethod();
        when(workRecordRepository.getPersonShareForBudget(1L)).thenReturn(createShares());

        List<Share> shares = service.getPeopleDistribution(1L);

        Assertions.assertThat(shares)
                .hasSize(4)
                .extracting(Share::getName, Share::getShare)
                .containsAll(Arrays.asList(
                        Tuple.tuple("share1", MoneyUtil.createMoneyFromCents(10000L)),
                        Tuple.tuple("share2", MoneyUtil.createMoneyFromCents(20000L)),
                        Tuple.tuple("share3", MoneyUtil.createMoneyFromCents(30000L)),
                        Tuple.tuple("share4", MoneyUtil.createMoneyFromCents(40000L))));
    }

    @Test
    void testGetWeekStatsForPerson() throws Exception {
        when(dateUtil.weeksAgo(5)).thenReturn(getWeeksAgoDate(format.parse("29.01.2015"), 5));
        when(workRecordRepository.aggregateByWeekAndBudgetForPerson(anyLong(), any(Date.class))).thenReturn(createLast5WeeksForBudget());
        when(planRecordRepository.aggregateByWeekForPerson(anyLong(), any(Date.class))).thenReturn(createLast5Weeks());

        TargetAndActual targetAndActual = service.getWeekStatsForPerson(1L, 5);

        List<Money> targetSeries = targetAndActual.getTargetSeries().getValues();
        Assertions.assertThat(targetSeries)
                .hasSize(5)
                .containsAll(Arrays.asList(MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L)));

        targetAndActual.getActualSeries().sort(moneySeriesComparator);

        Assertions.assertThat(targetAndActual.getActualSeries())
                .hasSize(2)
                .isSortedAccordingTo(moneySeriesComparator)
                .extracting(MoneySeries::getName, MoneySeries::getValues)
                .containsExactly(
                        Tuple.tuple("Budget 1", Arrays.asList(
                                MoneyUtil.createMoneyFromCents(100000L),
                                MoneyUtil.createMoneyFromCents(200000L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(400000L),
                                MoneyUtil.createMoneyFromCents(500000L))),
                        Tuple.tuple("Budget 2", Arrays.asList(
                                MoneyUtil.createMoneyFromCents(100000L),
                                MoneyUtil.createMoneyFromCents(200000L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(500000L))));
    }

    @Test
    void testGetWeekStatsForBudget() throws Exception {
        when(dateUtil.weeksAgo(5)).thenReturn(getWeeksAgoDate(format.parse("29.01.2015"), 5));
        when(workRecordRepository.aggregateByWeekAndPersonForBudget(anyLong(), any(Date.class))).thenReturn(createLast5WeeksForPerson());
        when(planRecordRepository.aggregateByWeekForBudget(anyLong(), any(Date.class))).thenReturn(createLast5Weeks());

        TargetAndActual targetAndActual = service.getWeekStatsForBudget(1L, 5);

        List<Money> targetSeries = targetAndActual.getTargetSeries().getValues();
        Assertions.assertThat(targetSeries)
                .hasSize(5)
                .containsAll(Arrays.asList(MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L)));

        targetAndActual.getActualSeries().sort(moneySeriesComparator);

        Assertions.assertThat(targetAndActual.getActualSeries())
                .hasSize(2)
                .isSortedAccordingTo(moneySeriesComparator)
                .extracting(MoneySeries::getName, MoneySeries::getValues)
                .containsExactly(
                        Tuple.tuple("Person 1", Arrays.asList(
                                MoneyUtil.createMoneyFromCents(100000L),
                                MoneyUtil.createMoneyFromCents(200000L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(400000L),
                                MoneyUtil.createMoneyFromCents(500000L))),
                        Tuple.tuple("Person 2", Arrays.asList(
                                MoneyUtil.createMoneyFromCents(100000L),
                                MoneyUtil.createMoneyFromCents(200000L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(500000L))));
    }

    @Test
    void testGetWeekStatsForBudgetWithTax() throws Exception {
        when(dateUtil.weeksAgo(5)).thenReturn(getWeeksAgoDate(format.parse("29.01.2015"), 5));
        List<WeeklyAggregatedRecordWithTitleAndTaxBean> burnedStats = ListJoiner.joinWorkBeanHours(createLast5WeeksForPersonWithTax());
        List<WeeklyAggregatedRecordWithTaxBean> planStats = ListJoiner.joinPlanBeanHours(createLast5WeeksWithTax());
        MonthlyStats monthlyStats = createMonthlyStatsForPeople();
        monthlyStats.sumPlanStats();
        monthlyStats.calculateCentValuesByMonthlyFraction(planStats, burnedStats);

        TargetAndActual targetAndActual = service.calculateWeeklyTargetAndActual(5, planStats, burnedStats);

        Assertions.assertThat(targetAndActual.getTargetSeries())
                .extracting(MoneySeries::getValues, MoneySeries::getValues_gross)
                .containsExactly(Arrays.asList(MoneyUtil.createMoneyFromCents(37500),
                        MoneyUtil.createMoneyFromCents(38125),
                        MoneyUtil.createMoneyFromCents(0),
                        MoneyUtil.createMoneyFromCents(58125),
                        MoneyUtil.createMoneyFromCents(38750)),
                        Arrays.asList(MoneyUtil.createMoneyFromCents(43125),
                                MoneyUtil.createMoneyFromCents(43875),
                                MoneyUtil.createMoneyFromCents(0),
                                MoneyUtil.createMoneyFromCents(65875),
                                MoneyUtil.createMoneyFromCents(44563)));

        targetAndActual.getActualSeries().sort(moneySeriesComparator);

        Assertions.assertThat(targetAndActual.getActualSeries())
                .hasSize(2)
                .isSortedAccordingTo(moneySeriesComparator)
                .extracting(MoneySeries::getName, MoneySeries::getValues, MoneySeries::getValues_gross)
                .containsExactly(Tuple.tuple("Person 1",
                        Arrays.asList(MoneyUtil.createMoneyFromCents(18750),
                                MoneyUtil.createMoneyFromCents(20000),
                                MoneyUtil.createMoneyFromCents(0),
                                MoneyUtil.createMoneyFromCents(57500),
                                MoneyUtil.createMoneyFromCents(19375)),
                        Arrays.asList(MoneyUtil.createMoneyFromCents(20625),
                                MoneyUtil.createMoneyFromCents(22000),
                                MoneyUtil.createMoneyFromCents(0),
                                MoneyUtil.createMoneyFromCents(67125),
                                MoneyUtil.createMoneyFromCents(21313))),
                        Tuple.tuple("Person 2",
                                Arrays.asList(MoneyUtil.createMoneyFromCents(38125),
                                        MoneyUtil.createMoneyFromCents(19375),
                                        MoneyUtil.createMoneyFromCents(0),
                                        MoneyUtil.createMoneyFromCents(19375),
                                        MoneyUtil.createMoneyFromCents(0)),
                                Arrays.asList(MoneyUtil.createMoneyFromCents(43813),
                                        MoneyUtil.createMoneyFromCents(21313),
                                        MoneyUtil.createMoneyFromCents(0),
                                        MoneyUtil.createMoneyFromCents(21313),
                                        MoneyUtil.createMoneyFromCents(0))));
    }

    @Test
    void testGetWeekStatsForBudgets() throws Exception {
        when(dateUtil.weeksAgo(5)).thenReturn(getWeeksAgoDate(format.parse("29.01.2015"), 5));
        when(workRecordRepository.aggregateByWeekAndPersonForBudgets(anyLong(), anyList(), any(Date.class))).thenReturn(createLast5WeeksForBudget());
        when(planRecordRepository.aggregateByWeekForBudgets(anyLong(), anyList(), any(Date.class))).thenReturn(createLast5Weeks());

        TargetAndActual targetAndActual = service.getWeekStatsForBudgets(new BudgetTagFilter(Collections.singletonList("tag1"), 1L), 5);

        Assertions.assertThat(targetAndActual.getTargetSeries().getValues())
                .hasSize(5)
                .containsExactly(MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L));

        targetAndActual.getActualSeries().sort(moneySeriesComparator);

        Assertions.assertThat(targetAndActual.getActualSeries())
                .hasSize(2)
                .isSortedAccordingTo(moneySeriesComparator)
                .extracting(MoneySeries::getName, MoneySeries::getValues)
                .containsExactly(Tuple.tuple("Budget 1", Arrays.asList(
                        MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L))),
                        Tuple.tuple("Budget 2", Arrays.asList(
                                MoneyUtil.createMoneyFromCents(100000L),
                                MoneyUtil.createMoneyFromCents(200000L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(500000L))));
    }

    @Test
    void testGetWeekStatsForBudgetsWithTax() throws Exception {
        when(dateUtil.weeksAgo(5)).thenReturn(getWeeksAgoDate(format.parse("29.01.2015"), 5));
        List<WeeklyAggregatedRecordWithTitleAndTaxBean> burnedStats = ListJoiner.joinWorkBeanHours(createLast5WeeksForBudgetWithTax());
        List<WeeklyAggregatedRecordWithTaxBean> planStats = ListJoiner.joinPlanBeanHours(createLast5WeeksWithTax());
        MonthlyStats monthlyStats = createMonthlyStatsForBudgets();
        monthlyStats.sumPlanStats();
        monthlyStats.calculateCentValuesByMonthlyFraction(planStats, burnedStats);

        TargetAndActual targetAndActual = service.calculateWeeklyTargetAndActual(5, planStats, burnedStats);

        Assertions.assertThat(targetAndActual.getTargetSeries())
                .extracting(MoneySeries::getValues, MoneySeries::getValues_gross)
                .containsExactly(Arrays.asList(MoneyUtil.createMoneyFromCents(37500),
                        MoneyUtil.createMoneyFromCents(38125),
                        MoneyUtil.createMoneyFromCents(0),
                        MoneyUtil.createMoneyFromCents(58125),
                        MoneyUtil.createMoneyFromCents(38750)),
                        Arrays.asList(MoneyUtil.createMoneyFromCents(43125),
                                MoneyUtil.createMoneyFromCents(43875),
                                MoneyUtil.createMoneyFromCents(0),
                                MoneyUtil.createMoneyFromCents(65875),
                                MoneyUtil.createMoneyFromCents(44563)));

        targetAndActual.getActualSeries().sort(moneySeriesComparator);

        Assertions.assertThat(targetAndActual.getActualSeries())
                .hasSize(2)
                .isSortedAccordingTo(moneySeriesComparator)
                .extracting(MoneySeries::getName, MoneySeries::getValues, MoneySeries::getValues_gross)
                .containsExactly(Tuple.tuple("Budget 1",
                        Arrays.asList(MoneyUtil.createMoneyFromCents(18750),
                                MoneyUtil.createMoneyFromCents(20000),
                                MoneyUtil.createMoneyFromCents(0),
                                MoneyUtil.createMoneyFromCents(57500),
                                MoneyUtil.createMoneyFromCents(19375)),
                        Arrays.asList(MoneyUtil.createMoneyFromCents(20625),
                                MoneyUtil.createMoneyFromCents(22000),
                                MoneyUtil.createMoneyFromCents(0),
                                MoneyUtil.createMoneyFromCents(67125),
                                MoneyUtil.createMoneyFromCents(21313))),
                        Tuple.tuple("Budget 2",
                                Arrays.asList(MoneyUtil.createMoneyFromCents(38125),
                                        MoneyUtil.createMoneyFromCents(19375),
                                        MoneyUtil.createMoneyFromCents(0),
                                        MoneyUtil.createMoneyFromCents(19375),
                                        MoneyUtil.createMoneyFromCents(0)),
                                Arrays.asList(MoneyUtil.createMoneyFromCents(43813),
                                        MoneyUtil.createMoneyFromCents(21313),
                                        MoneyUtil.createMoneyFromCents(0),
                                        MoneyUtil.createMoneyFromCents(21313),
                                        MoneyUtil.createMoneyFromCents(0))));
    }

    @Test
    void testGetMonthStatsForPerson() throws Exception {
        when(dateUtil.monthsAgo(5)).thenReturn(getMonthsAgoDate(format.parse("29.01.2015"), 5));
        when(workRecordRepository.aggregateByMonthAndBudgetForPerson(anyLong(), any(Date.class))).thenReturn(createLast5MonthsForBudget());
        when(planRecordRepository.aggregateByMonthForPerson(anyLong(), any(Date.class))).thenReturn(createLast5Months());

        TargetAndActual targetAndActual = service.getMonthStatsForPerson(1L, 5);


        Assertions.assertThat(targetAndActual.getTargetSeries().getValues())
                .hasSize(5)
                .containsExactly(MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L));

        targetAndActual.getActualSeries().sort(moneySeriesComparator);

        Assertions.assertThat(targetAndActual.getActualSeries())
                .hasSize(2)
                .isSortedAccordingTo(moneySeriesComparator)
                .extracting(MoneySeries::getName, MoneySeries::getValues)
                .containsExactly(Tuple.tuple("Budget 1", Arrays.asList(
                        MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L))),
                        Tuple.tuple("Budget 2", Arrays.asList(
                                MoneyUtil.createMoneyFromCents(100000L),
                                MoneyUtil.createMoneyFromCents(200000L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(500000L))));
    }

    @Test
    void testGetMonthStatsForBudgets() throws Exception {
        when(dateUtil.monthsAgo(5)).thenReturn(getMonthsAgoDate(format.parse("29.01.2015"), 5));
        when(workRecordRepository.aggregateByMonthAndPersonForBudgets(anyLong(), anyList(), any(Date.class))).thenReturn(createLast5MonthsForBudget());
        when(planRecordRepository.aggregateByMonthForBudgets(anyLong(), anyList(), any(Date.class))).thenReturn(createLast5Months());

        TargetAndActual targetAndActual = service.getMonthStatsForBudgets(new BudgetTagFilter(Collections.singletonList("tag1"), 1L), 5);

        Assertions.assertThat(targetAndActual.getTargetSeries().getValues())
                .hasSize(5)
                .containsExactly(MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L));

        targetAndActual.getActualSeries().sort(moneySeriesComparator);

        Assertions.assertThat(targetAndActual.getActualSeries())
                .hasSize(2)
                .isSortedAccordingTo(moneySeriesComparator)
                .extracting(MoneySeries::getName, MoneySeries::getValues)
                .containsExactly(Tuple.tuple("Budget 1", Arrays.asList(
                        MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L))),
                        Tuple.tuple("Budget 2", Arrays.asList(
                                MoneyUtil.createMoneyFromCents(100000L),
                                MoneyUtil.createMoneyFromCents(200000L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(500000L))));
    }

    @Test
    void testGetMonthStatsForBudget() throws Exception {
        when(dateUtil.monthsAgo(5)).thenReturn(getMonthsAgoDate(format.parse("29.01.2015"), 5));
        when(workRecordRepository.aggregateByMonthAndPersonForBudget(anyLong(), any(Date.class))).thenReturn(createLast5MonthsForBudget());
        when(planRecordRepository.aggregateByMonthForBudget(anyLong(), any(Date.class))).thenReturn(createLast5Months());

        TargetAndActual targetAndActual = service.getMonthStatsForBudget(1L, 5);

        Assertions.assertThat(targetAndActual.getTargetSeries().getValues())
                .hasSize(5)
                .containsExactly(MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L));

        targetAndActual.getActualSeries().sort(moneySeriesComparator);

        Assertions.assertThat(targetAndActual.getActualSeries())
                .hasSize(2)
                .isSortedAccordingTo(moneySeriesComparator)
                .extracting(MoneySeries::getName, MoneySeries::getValues)
                .containsExactly(Tuple.tuple("Budget 1", Arrays.asList(
                        MoneyUtil.createMoneyFromCents(100000L),
                        MoneyUtil.createMoneyFromCents(200000L),
                        MoneyUtil.createMoneyFromCents(0L),
                        MoneyUtil.createMoneyFromCents(400000L),
                        MoneyUtil.createMoneyFromCents(500000L))),
                        Tuple.tuple("Budget 2", Arrays.asList(
                                MoneyUtil.createMoneyFromCents(100000L),
                                MoneyUtil.createMoneyFromCents(200000L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(0L),
                                MoneyUtil.createMoneyFromCents(500000L))));
    }

    @Test
    void testFillMissingMonths() throws ParseException {
        when(dateUtil.monthsAgo(5)).thenReturn(getMonthsAgoDate(format.parse("01.05.2015"), 5));
        List<MonthlyAggregatedRecordBean> beans = Arrays.asList(
                new MonthlyAggregatedRecordBean(2015, 1, 15d, 100000),
                new MonthlyAggregatedRecordBean(2015, 2, 15d, 200000),
                new MonthlyAggregatedRecordBean(2015, 4, 15d, 400000),
                new MonthlyAggregatedRecordBean(2015, 5, 15d, 500000)
        );
        List<Money> testList = new ArrayList<>();

        service.fillMissingMonths(5, beans, testList);

        Assertions.assertThat(testList)
                .hasSize(5)
                .containsExactly(MoneyUtil.createMoneyFromCents(0),
                        MoneyUtil.createMoneyFromCents(100000),
                        MoneyUtil.createMoneyFromCents(200000),
                        MoneyUtil.createMoneyFromCents(0),
                        MoneyUtil.createMoneyFromCents(400000));
    }

    private MonthlyStats createMonthlyStatsForBudgets() {
        List<MonthlyAggregatedRecordWithTaxBean> planStats = new ArrayList<>();
        planStats.add(new MonthlyAggregatedRecordWithTaxBean(2015, 1, 2730, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        planStats.add(new MonthlyAggregatedRecordWithTaxBean(2015, 1, 2760, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20)));
        planStats.add(new MonthlyAggregatedRecordWithTaxBean(2015, 2, 1870, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        planStats.add(new MonthlyAggregatedRecordWithTaxBean(2015, 2, 940, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20)));

        List<MonthlyAggregatedRecordWithTitleAndTaxBean> workStats = new ArrayList<>();
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 1, 2770, MoneyUtil.createMoneyFromCents(10000), "Budget 1", BigDecimal.valueOf(10)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 1, 930, MoneyUtil.createMoneyFromCents(10000), "Budget 1", BigDecimal.valueOf(20)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 1, 1850, MoneyUtil.createMoneyFromCents(10000), "Budget 2", BigDecimal.valueOf(10)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 1, 910, MoneyUtil.createMoneyFromCents(10000), "Budget 2", BigDecimal.valueOf(10)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 2, 920, MoneyUtil.createMoneyFromCents(10000), "Budget 1", BigDecimal.valueOf(10)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 2, 940, MoneyUtil.createMoneyFromCents(10000), "Budget 1", BigDecimal.valueOf(20)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 2, 940, MoneyUtil.createMoneyFromCents(10000), "Budget 2", BigDecimal.valueOf(10)));

        return new MonthlyStats(planStats, workStats);
    }

    private MonthlyStats createMonthlyStatsForPeople() {
        List<MonthlyAggregatedRecordWithTaxBean> planStats = new ArrayList<>();
        planStats.add(new MonthlyAggregatedRecordWithTaxBean(2015, 1, 2730, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        planStats.add(new MonthlyAggregatedRecordWithTaxBean(2015, 1, 2760, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20)));
        planStats.add(new MonthlyAggregatedRecordWithTaxBean(2015, 2, 1870, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        planStats.add(new MonthlyAggregatedRecordWithTaxBean(2015, 2, 940, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20)));

        List<MonthlyAggregatedRecordWithTitleAndTaxBean> workStats = new ArrayList<>();
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 1, 2770, MoneyUtil.createMoneyFromCents(10000), "Person 1", BigDecimal.valueOf(10)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 1, 930, MoneyUtil.createMoneyFromCents(10000), "Person 1", BigDecimal.valueOf(20)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 1, 1850, MoneyUtil.createMoneyFromCents(10000), "Person 2", BigDecimal.valueOf(10)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 1, 910, MoneyUtil.createMoneyFromCents(10000), "Person 2", BigDecimal.valueOf(10)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 2, 920, MoneyUtil.createMoneyFromCents(10000), "Person 1", BigDecimal.valueOf(10)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 2, 940, MoneyUtil.createMoneyFromCents(10000), "Person 1", BigDecimal.valueOf(20)));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2015, 2, 940, MoneyUtil.createMoneyFromCents(10000), "Person 2", BigDecimal.valueOf(10)));

        return new MonthlyStats(planStats, workStats);
    }

    private List<WeeklyAggregatedRecordWithTitleBean> createLast5WeeksForBudget() {
        List<WeeklyAggregatedRecordWithTitleBean> beans = new ArrayList<>();
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 1, 15d, 100000, "Budget 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 2, 15d, 200000, "Budget 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 4, 15d, 400000, "Budget 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 5, 15d, 500000, "Budget 1"));

        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 1, 15d, 100000, "Budget 2"));
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 2, 15d, 200000, "Budget 2"));
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 5, 15d, 500000, "Budget 2"));
        return beans;
    }

    private List<WeeklyAggregatedRecordWithTitleAndTaxBean> createLast5WeeksForBudgetWithTax() {
        List<WeeklyAggregatedRecordWithTitleAndTaxBean> beans = new ArrayList<>();
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 1, 900, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Budget 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 2, 960, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Budget 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 4, 910, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Budget 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 2, 5, 920, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Budget 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 4, 930, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20), "Budget 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 2, 4, 940, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20), "Budget 1"));

        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 1, 910, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20), "Budget 2"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 1, 920, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Budget 2"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 2, 930, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Budget 2"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 2, 4, 940, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Budget 2"));
        return beans;
    }

    private List<MonthlyAggregatedRecordWithTitleBean> createLast5MonthsForBudget() {
        List<MonthlyAggregatedRecordWithTitleBean> beans = new ArrayList<>();
        beans.add(new MonthlyAggregatedRecordWithTitleBean(2014, 8, 15d, 100000, "Budget 1"));
        beans.add(new MonthlyAggregatedRecordWithTitleBean(2014, 9, 15d, 200000, "Budget 1"));
        beans.add(new MonthlyAggregatedRecordWithTitleBean(2014, 11, 15d, 400000, "Budget 1"));
        beans.add(new MonthlyAggregatedRecordWithTitleBean(2015, 0, 15d, 500000, "Budget 1"));

        beans.add(new MonthlyAggregatedRecordWithTitleBean(2014, 8, 15d, 100000, "Budget 2"));
        beans.add(new MonthlyAggregatedRecordWithTitleBean(2014, 9, 15d, 200000, "Budget 2"));
        beans.add(new MonthlyAggregatedRecordWithTitleBean(2015, 0, 15d, 500000, "Budget 2"));
        return beans;
    }

    private List<WeeklyAggregatedRecordWithTitleBean> createLast5WeeksForPerson() {
        List<WeeklyAggregatedRecordWithTitleBean> beans = new ArrayList<>();
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 1, 15d, 100000, "Person 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 2, 15d, 200000, "Person 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 4, 15d, 400000, "Person 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 5, 15d, 500000, "Person 1"));

        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 1, 15d, 100000, "Person 2"));
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 2, 15d, 200000, "Person 2"));
        beans.add(new WeeklyAggregatedRecordWithTitleBean(2015, 5, 15d, 500000, "Person 2"));
        return beans;
    }

    private List<WeeklyAggregatedRecordWithTitleAndTaxBean> createLast5WeeksForPersonWithTax() {
        List<WeeklyAggregatedRecordWithTitleAndTaxBean> beans = new ArrayList<>();
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 1, 900, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Person 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 2, 960, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Person 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 4, 910, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Person 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 2, 5, 920, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Person 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 4, 930, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20), "Person 1"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 2, 4, 940, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20), "Person 1"));

        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 1, 910, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20), "Person 2"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 1, 920, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Person 2"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 1, 2, 930, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Person 2"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2015, 2, 4, 940, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10), "Person 2"));
        return beans;
    }

    private List<WeeklyAggregatedRecordBean> createLast5Weeks() {
        List<WeeklyAggregatedRecordBean> beans = new ArrayList<>();
        beans.add(new WeeklyAggregatedRecordBean(2015, 1, 15d, 100000));
        beans.add(new WeeklyAggregatedRecordBean(2015, 2, 15d, 200000));
        beans.add(new WeeklyAggregatedRecordBean(2015, 4, 15d, 400000));
        beans.add(new WeeklyAggregatedRecordBean(2015, 5, 15d, 500000));
        return beans;
    }

    private List<WeeklyAggregatedRecordWithTaxBean> createLast5WeeksWithTax() {
        List<WeeklyAggregatedRecordWithTaxBean> beans = new ArrayList<>();
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2015, 1, 1, 900, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2015, 1, 2, 910, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2015, 1, 4, 920, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2015, 2, 4, 930, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2015, 2, 5, 940, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));

        beans.add(new WeeklyAggregatedRecordWithTaxBean(2015, 1, 1, 910, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20)));
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2015, 1, 2, 920, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20)));
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2015, 1, 4, 930, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20)));
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2015, 2, 5, 940, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(20)));
        return beans;
    }

    private List<MonthlyAggregatedRecordBean> createLast5Months() {
        List<MonthlyAggregatedRecordBean> beans = new ArrayList<>();
        beans.add(new MonthlyAggregatedRecordBean(2014, 8, 15d, 100000));
        beans.add(new MonthlyAggregatedRecordBean(2014, 9, 15d, 200000));
        beans.add(new MonthlyAggregatedRecordBean(2014, 11, 15d, 400000));
        beans.add(new MonthlyAggregatedRecordBean(2015, 0, 15d, 500000));
        return beans;
    }

    private List<DailyAverageRateBean> createLast5Days() {
        List<DailyAverageRateBean> beans = new ArrayList<>();
        beans.add(new DailyAverageRateBean(2015, 0, 1, 100d));
        beans.add(new DailyAverageRateBean(2015, 0, 2, 200d));
        beans.add(new DailyAverageRateBean(2015, 0, 4, 400d));
        beans.add(new DailyAverageRateBean(2015, 0, 5, 500d));
        return beans;
    }

    private Date getMonthsAgoDate(Date currentDate, int numberOfMonths) {
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.MONTH, -numberOfMonths + 1); // +1, because we want to have the current month included
        return c.getTime();
    }

    private Date getWeeksAgoDate(Date currentDate, int numberOfWeeks) {
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.WEEK_OF_YEAR, -numberOfWeeks + 1); // +1, because we want to have the current week included
        return c.getTime();
    }

    private Date getDaysAgoDate(Date currentDate, int numberOfDays) {
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DAY_OF_YEAR, -numberOfDays + 1); // +1, because we want to have the current day included
        return c.getTime();
    }


    private List<ShareBean> createShares() {
        List<ShareBean> shares = new ArrayList<>();
        shares.add(new ShareBean("share1", 10000L));
        shares.add(new ShareBean("share2", 20000L));
        shares.add(new ShareBean("share3", 30000L));
        shares.add(new ShareBean("share4", 40000L));
        return shares;
    }
}
