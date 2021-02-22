package org.wickedsource.budgeteer.service.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.budget.MissingBudgetTotalBean;
import org.wickedsource.budgeteer.persistence.record.MissingDailyRateBean;
import org.wickedsource.budgeteer.persistence.record.MissingDailyRateForBudgetBean;
import org.wickedsource.budgeteer.persistence.record.PlanRecordRepository;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.persistence.user.UserRepository;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService service;
    @Mock
    private WorkRecordRepository workRecordRepository;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private PlanRecordRepository planRecordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MissingDailyRateNotificationMapper missingDailyRateMapper;
    @Mock
    private MissingBudgetTotalNotificationMapper missingBudgetTotalNotificationMapper;
    @Mock
    private LimitReachedNotificationMapper limitReachedNotificationMapper;
    @Mock
    private MissingDailyRateForBudgetNotificationMapper missingDailyRateForBudgetNotificationMapper;

    private final Date fixedDate = new Date();

    @Test
    void testGetNotifications() {
        when(workRecordRepository.getMissingDailyRatesForProject(1L)).thenReturn(Collections.singletonList(createMissingDailyRate()));
        when(workRecordRepository.countByProjectId(anyLong())).thenReturn(0L, 0L);
        when(budgetRepository.getMissingBudgetTotalsForProject(1L)).thenReturn(Collections.singletonList(createMissingBudgetTotal()));
        when(planRecordRepository.countByProjectId(1L)).thenReturn(0L);
        when(missingDailyRateMapper.map(anyList())).thenCallRealMethod();
        when(missingBudgetTotalNotificationMapper.map(anyList())).thenCallRealMethod();

        List<Notification> notifications = service.getNotifications(1L, 1L);

        Assertions.assertThat(notifications)
                .hasSize(4);
    }

    @Test
    void testGetNotificationsForPerson() {
        when(workRecordRepository.getMissingDailyRatesForPerson(1L)).thenReturn(Collections.singletonList(createMissingDailyRateForBudget()));
        when(missingDailyRateForBudgetNotificationMapper.map(anyList())).thenCallRealMethod();
        when(missingDailyRateForBudgetNotificationMapper.map(any(MissingDailyRateForBudgetBean.class))).thenCallRealMethod();

        List<Notification> notifications = service.getNotificationsForPerson(1L);

        Assertions.assertThat(notifications)
                .hasSize(1)
                .containsExactly(new MissingDailyRateForBudgetNotification()
                        .setPersonId(1L)
                        .setPersonName("person1")
                        .setStartDate(fixedDate)
                        .setEndDate(fixedDate)
                        .setBudgetName("Budget1"));
    }

    @Test
    void testGetNotificationsForBudget() {
        when(budgetRepository.getMissingBudgetTotalForBudget(1L)).thenReturn(createMissingBudgetTotal());
        when(missingBudgetTotalNotificationMapper.map(any(MissingBudgetTotalBean.class))).thenCallRealMethod();

        List<Notification> notifications = service.getNotificationsForBudget(1L);

        Assertions.assertThat(notifications)
                .hasSize(1)
                .containsExactly(new MissingBudgetTotalNotification()
                        .setBudgetId(1L)
                        .setBudgetName("budget1"));
    }

    private MissingDailyRateBean createMissingDailyRate() {
        return new MissingDailyRateBean(1L, "person1", fixedDate, fixedDate);
    }

    private MissingDailyRateForBudgetBean createMissingDailyRateForBudget() {
        return new MissingDailyRateForBudgetBean(1L, "person1", fixedDate, fixedDate, "Budget1", 1L);
    }

    private MissingBudgetTotalBean createMissingBudgetTotal() {
        return new MissingBudgetTotalBean(1L, "budget1");
    }
}
