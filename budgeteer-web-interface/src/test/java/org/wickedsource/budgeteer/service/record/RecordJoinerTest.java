package org.wickedsource.budgeteer.service.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.record.*;
import org.wickedsource.budgeteer.service.statistics.MonthlyStats;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordJoinerTest {

    @Mock
    private RecordJoiner joiner;

    private DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Test
    void testJoinWeekly() throws Exception {
        when(joiner.joinWeekly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenCallRealMethod();

        List<AggregatedRecord> records = joiner.joinWeekly(createWeeklyWorkRecords(), createWeeklyPlanRecords());

        Assertions.assertThat(records)
                .hasSize(3)
                .containsExactly(new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("03.03.2014"))
                                .setAggregationPeriodEnd(format.parse("09.03.2014"))
                                .setAggregationPeriodTitle("Week 2014-10")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(50000))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(12300))
                                .setHours(5d),
                        new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("06.04.2015"))
                                .setAggregationPeriodEnd(format.parse("12.04.2015"))
                                .setAggregationPeriodTitle("Week 2015-15")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(50000))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(12300))
                                .setHours(5d),
                        new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("13.04.2015"))
                                .setAggregationPeriodEnd(format.parse("19.04.2015"))
                                .setAggregationPeriodTitle("Week 2015-16")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(60000))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(32100))
                                .setHours(6d)
                );
    }

    @Test
    void testJoinWeeklyWithTax() throws Exception {
        when(joiner.joinWeeklyByMonthFraction(ArgumentMatchers.anyList(), ArgumentMatchers.anyList(), any())).thenCallRealMethod();

        List<AggregatedRecord> recordsWithTax = joiner.joinWeeklyByMonthFraction(createWeeklyWorkRecordsWithTax(), createWeeklyPlanRecordsWithTax(), createMonthlyStats());

        Assertions.assertThat(recordsWithTax)
                .hasSize(2)
                .containsExactly(new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("26.03.2018"))
                                .setAggregationPeriodEnd(format.parse("1.04.2018"))
                                .setAggregationPeriodTitle("Week 2018-13")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(16250))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(18750))
                                .setBudgetBurned_gross(MoneyUtil.createMoneyFromCents(17625))
                                .setBudgetPlanned_gross(MoneyUtil.createMoneyFromCents(20313))
                                .setHours(13d),
                        new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("02.04.2018"))
                                .setAggregationPeriodEnd(format.parse("08.04.2018"))
                                .setAggregationPeriodTitle("Week 2018-14")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(3750))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(5000))
                                .setBudgetBurned_gross(MoneyUtil.createMoneyFromCents(4125))
                                .setBudgetPlanned_gross(MoneyUtil.createMoneyFromCents(5500))
                                .setHours(3d)
                );
    }

    @Test
    void testJoinMonthly() throws Exception {
        when(joiner.joinMonthly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenCallRealMethod();

        List<AggregatedRecord> records = joiner.joinMonthly(createMonthlyWorkRecords(), createMonthlyPlanRecords());

        Assertions.assertThat(records)
                .hasSize(3)
                .containsExactly(new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("01.01.2014"))
                                .setAggregationPeriodEnd(format.parse("31.01.2014"))
                                .setAggregationPeriodTitle("Month 2014-01")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(50000))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(12300))
                                .setHours(5d),
                        new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("01.06.2015"))
                                .setAggregationPeriodEnd(format.parse("30.06.2015"))
                                .setAggregationPeriodTitle("Month 2015-06")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(50000))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(12300))
                                .setHours(5d),
                        new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("01.07.2015"))
                                .setAggregationPeriodEnd(format.parse("31.07.2015"))
                                .setAggregationPeriodTitle("Month 2015-07")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(60000))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(32100))
                                .setHours(6d)
                );
    }

    @Test
    void testJoinMonthlyWithTax() throws Exception {
        when(joiner.joinMonthlyWithTax(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenCallRealMethod();

        List<AggregatedRecord> records = joiner.joinMonthlyWithTax(createMonthlyWorkRecordsWithTax(), createMonthlyPlanRecordsWithTax());

        Assertions.assertThat(records)
                .hasSize(6)
                .containsExactly(new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("01.01.2014"))
                                .setAggregationPeriodEnd(format.parse("31.01.2014"))
                                .setAggregationPeriodTitle("Month 2014-01")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(100000))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(12300))
                                .setBudgetBurned_gross(MoneyUtil.createMoneyFromCents(107500))
                                .setBudgetPlanned_gross(MoneyUtil.createMoneyFromCents(13530))
                                .setHours(10d),
                        new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("01.06.2015"))
                                .setAggregationPeriodEnd(format.parse("30.06.2015"))
                                .setAggregationPeriodTitle("Month 2015-06")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(50000))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(12300))
                                .setBudgetBurned_gross(MoneyUtil.createMoneyFromCents(55000))
                                .setBudgetPlanned_gross(MoneyUtil.createMoneyFromCents(13530))
                                .setHours(5d),
                        new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("01.07.2015"))
                                .setAggregationPeriodEnd(format.parse("31.07.2015"))
                                .setAggregationPeriodTitle("Month 2015-07")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(60000))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(20000))
                                .setBudgetBurned_gross(MoneyUtil.createMoneyFromCents(66000))
                                .setBudgetPlanned_gross(MoneyUtil.createMoneyFromCents(21500))
                                .setHours(6d),
                        new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("01.01.2016"))
                                .setAggregationPeriodEnd(format.parse("31.01.2016"))
                                .setAggregationPeriodTitle("Month 2016-01")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(143750))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(156250))
                                .setBudgetBurned_gross(MoneyUtil.createMoneyFromCents(152219))
                                .setBudgetPlanned_gross(MoneyUtil.createMoneyFromCents(166157))
                                .setHours(115d),
                        new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("01.06.2016"))
                                .setAggregationPeriodEnd(format.parse("30.06.2016"))
                                .setAggregationPeriodTitle("Month 2016-06")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(201250))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(208125))
                                .setBudgetBurned_gross(MoneyUtil.createMoneyFromCents(221375))
                                .setBudgetPlanned_gross(MoneyUtil.createMoneyFromCents(228938))
                                .setHours(161d),
                        new AggregatedRecord()
                                .setAggregationPeriodStart(format.parse("01.07.2016"))
                                .setAggregationPeriodEnd(format.parse("31.07.2016"))
                                .setAggregationPeriodTitle("Month 2016-07")
                                .setBudgetBurned_net(MoneyUtil.createMoneyFromCents(200000))
                                .setBudgetPlanned_net(MoneyUtil.createMoneyFromCents(208125))
                                .setBudgetBurned_gross(MoneyUtil.createMoneyFromCents(220000))
                                .setBudgetPlanned_gross(MoneyUtil.createMoneyFromCents(228938))
                                .setHours(160d)
                );
    }

    private List<WeeklyAggregatedRecordBean> createWeeklyWorkRecords() {
        List<WeeklyAggregatedRecordBean> beans = new ArrayList<>();
        beans.add(new WeeklyAggregatedRecordBean(2015, 15, 5d, 50000));
        beans.add(new WeeklyAggregatedRecordBean(2015, 16, 6d, 60000));
        beans.add(new WeeklyAggregatedRecordBean(2014, 10, 5d, 50000));
        return beans;
    }

    private List<WeeklyAggregatedRecordWithTitleAndTaxBean> createWeeklyWorkRecordsWithTax() {
        List<WeeklyAggregatedRecordWithTitleAndTaxBean> beans = new ArrayList<>();
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2018, 3, 13, 180, MoneyUtil.createMoneyFromCents(10000), BigDecimal.TEN, "Max Mustermann"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2018, 3, 13, 180, MoneyUtil.createMoneyFromCents(10000), BigDecimal.TEN, "Maria Mustermann"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2018, 4, 13, 180, MoneyUtil.createMoneyFromCents(10000), BigDecimal.TEN, "Max Mustermann"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2018, 4, 13, 240, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(5), "Max Mustermann"));
        beans.add(new WeeklyAggregatedRecordWithTitleAndTaxBean(2018, 4, 14, 180, MoneyUtil.createMoneyFromCents(10000), BigDecimal.TEN, "Max Mustermann"));
        return beans;
    }

    private List<WeeklyAggregatedRecordBean> createWeeklyPlanRecords() {
        List<WeeklyAggregatedRecordBean> beans = new ArrayList<>();
        beans.add(new WeeklyAggregatedRecordBean(2015, 15, 5d, 12300));
        beans.add(new WeeklyAggregatedRecordBean(2014, 10, 5d, 12300));
        beans.add(new WeeklyAggregatedRecordBean(2015, 16, 6d, 32100));
        return beans;
    }

    private List<WeeklyAggregatedRecordWithTaxBean> createWeeklyPlanRecordsWithTax() {
        List<WeeklyAggregatedRecordWithTaxBean> beans = new ArrayList<>();
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2018, 3, 13, 300, MoneyUtil.createMoneyFromCents(10000), BigDecimal.TEN));
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2018, 4, 13, 300, MoneyUtil.createMoneyFromCents(10000), BigDecimal.TEN));
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2018, 4, 13, 300, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(5)));
        beans.add(new WeeklyAggregatedRecordWithTaxBean(2018, 4, 14, 240, MoneyUtil.createMoneyFromCents(10000), BigDecimal.TEN));
        return beans;
    }

    private MonthlyStats createMonthlyStats() {
        List<MonthlyAggregatedRecordWithTaxBean> planStats = new ArrayList<>();
        planStats.add(new MonthlyAggregatedRecordWithTaxBean(2018, 3, 300, MoneyUtil.createMoneyFromCents(10000), BigDecimal.TEN));
        planStats.add(new MonthlyAggregatedRecordWithTaxBean(2018, 4, 540, MoneyUtil.createMoneyFromCents(10000), BigDecimal.TEN));
        planStats.add(new MonthlyAggregatedRecordWithTaxBean(2018, 4, 300, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(5)));

        List<MonthlyAggregatedRecordWithTitleAndTaxBean> workStats = new ArrayList<>();
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2018, 3, 180, MoneyUtil.createMoneyFromCents(10000), "Max Mustermann", BigDecimal.TEN));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2018, 3, 180, MoneyUtil.createMoneyFromCents(10000), "Maria Mustermann", BigDecimal.TEN));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2018, 4, 360, MoneyUtil.createMoneyFromCents(10000), "Max Mustermann", BigDecimal.TEN));
        workStats.add(new MonthlyAggregatedRecordWithTitleAndTaxBean(2018, 4, 240, MoneyUtil.createMoneyFromCents(10000), "Max Mustermann", BigDecimal.valueOf(5)));

        return new MonthlyStats(planStats, workStats);
    }

    private List<MonthlyAggregatedRecordBean> createMonthlyWorkRecords() {
        List<MonthlyAggregatedRecordBean> beans = new ArrayList<>();
        beans.add(new MonthlyAggregatedRecordBean(2015, 5, 5d, 50000));
        beans.add(new MonthlyAggregatedRecordBean(2015, 6, 6d, 60000));
        beans.add(new MonthlyAggregatedRecordBean(2014, 0, 5d, 50000));
        return beans;
    }

    private List<MonthlyAggregatedRecordBean> createMonthlyPlanRecords() {
        List<MonthlyAggregatedRecordBean> beans = new ArrayList<>();
        beans.add(new MonthlyAggregatedRecordBean(2015, 5, 5d, 12300));
        beans.add(new MonthlyAggregatedRecordBean(2014, 0, 5d, 12300));
        beans.add(new MonthlyAggregatedRecordBean(2015, 6, 6d, 32100));
        return beans;
    }

    private List<MonthlyAggregatedRecordWithTaxBean> createMonthlyWorkRecordsWithTax() {
        List<MonthlyAggregatedRecordWithTaxBean> beans = new ArrayList<>();
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2015, 5, 5d, 50000, BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2015, 6, 6d, 60000, BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2014, 0, 5d, 50000, BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2014, 0, 5d, 50000, BigDecimal.valueOf(5)));

        beans.add(new MonthlyAggregatedRecordWithTaxBean(2016, 5, 9656, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2016, 6, 9600, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2016, 0, 1234, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2016, 0, 5678, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(5)));

        return beans;
    }

    private List<MonthlyAggregatedRecordWithTaxBean> createMonthlyPlanRecordsWithTax() {
        List<MonthlyAggregatedRecordWithTaxBean> beans = new ArrayList<>();
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2015, 5, 5d, 12300, BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2014, 0, 5d, 12300, BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2015, 6, 6d, 10000, BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2015, 6, 6d, 10000, BigDecimal.valueOf(5)));

        beans.add(new MonthlyAggregatedRecordWithTaxBean(2016, 5, 10000, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2016, 6, 10000, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2016, 0, 2000, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(10)));
        beans.add(new MonthlyAggregatedRecordWithTaxBean(2016, 0, 5500, MoneyUtil.createMoneyFromCents(10000), BigDecimal.valueOf(5)));
        return beans;
    }


}
