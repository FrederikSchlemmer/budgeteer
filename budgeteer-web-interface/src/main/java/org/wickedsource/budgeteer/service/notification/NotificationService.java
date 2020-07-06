package org.wickedsource.budgeteer.service.notification;

import lombok.RequiredArgsConstructor;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.budget.LimitReachedBean;
import org.wickedsource.budgeteer.persistence.budget.MissingBudgetTotalBean;
import org.wickedsource.budgeteer.persistence.record.MissingDailyRateForBudgetBean;
import org.wickedsource.budgeteer.persistence.record.PlanRecordRepository;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.persistence.user.UserEntity;
import org.wickedsource.budgeteer.persistence.user.UserRepository;
import org.wickedsource.budgeteer.service.record.RecordService;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final WorkRecordRepository workRecordRepository;
    private final PlanRecordRepository planRecordRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final RecordService recordService;
    private final MissingDailyRateNotificationMapper missingDailyRateMapper;
    private final MissingBudgetTotalNotificationMapper missingBudgetTotalNotificationMapper;
    private final LimitReachedNotificationMapper limitReachedNotificationMapper;
    private final MissingDailyRateForBudgetNotificationMapper missingDailyRateForBudgetNotificationMapper;

    /**
     * Returns all notifications currently available for the given project
     *
     * @param projectId ID of the project whose notifications to load
     * @return list of notifications
     */
    public List<Notification> getNotifications(long projectId, long userId) {
        List<Notification> notifications = new ArrayList<>();
        if (workRecordRepository.countByProjectId(projectId) == 0) {
            notifications.add(new EmptyWorkRecordsNotification());
        }
        if (planRecordRepository.countByProjectId(projectId) == 0) {
            notifications.add(new EmptyPlanRecordsNotification());
        }
        notifications.addAll(budgetRepository.getMissingContractForProject(projectId));
        notifications.addAll(missingDailyRateMapper.map(recordService.getMissingDailyRatesForProject(projectId)));
        notifications.addAll(missingBudgetTotalNotificationMapper.map(budgetRepository.getMissingBudgetTotalsForProject(projectId)));

        List<LimitReachedBean> beansList = budgetRepository.getBudgetsForProject(projectId);
        for (LimitReachedBean bean : beansList) {
            Double spentDouble = workRecordRepository.getSpentBudget(bean.getBudgetId());
            if (spentDouble != null) {
                Money budgetSpent = MoneyUtil.createMoneyFromCents(Math.round(spentDouble));
                LimitReachedBean limitReached = budgetRepository.getLimitReachedForBudget(bean.getBudgetId(), budgetSpent);
                if (limitReached != null)
                    notifications.add(limitReachedNotificationMapper.map(limitReached));
            }
        }

        UserEntity user = userRepository.findOne(userId);
        if (user != null) {
            if (user.getMail() == null) {
                notifications.add(new MissingMailNotification(user.getId()));
            }

            if (!user.getMailVerified() && user.getMail() != null) {
                notifications.add(new MailNotVerifiedNotification(user.getId(), user.getMail()));
            }
        }

        return notifications;
    }

    /**
     * Returns all notifications currently available concerning the given person.
     *
     * @param personId id of the person about whom notifications should be returned.
     * @return list of notifications concerning the given person.
     */
    public List<Notification> getNotificationsForPerson(long personId) {
        List<MissingDailyRateForBudgetBean> missingDailyRatesForPerson = recordService.getMissingDailyRatesForPerson(personId);
        return missingDailyRateForBudgetNotificationMapper.map(missingDailyRatesForPerson);
    }



    /**
     * Returns all notifications currently available concerning the given budget.
     *
     * @param budgetId id of the budget about which notifications should be returned.
     * @return list of notifications concerning the given budget.
     */
    public List<Notification> getNotificationsForBudget(long budgetId) {
        List<Notification> result = new LinkedList<>();

        MissingBudgetTotalBean missingBudgetTotalForBudget = budgetRepository.getMissingBudgetTotalForBudget(budgetId);
        if (missingBudgetTotalForBudget != null) {
            result.add(missingBudgetTotalNotificationMapper.map(missingBudgetTotalForBudget));
        }

        MissingContractForBudgetNotification missingContract = budgetRepository.getMissingContractForBudget(budgetId);
        if (missingContract != null) {
            result.add(missingContract);
        }

        Double spentDouble = workRecordRepository.getSpentBudget(budgetId);
        if (spentDouble != null) {
            Money budgetSpent = MoneyUtil.createMoneyFromCents(Math.round(spentDouble));
            LimitReachedBean limitReached = budgetRepository.getLimitReachedForBudget(budgetId, budgetSpent);
            if (limitReached != null)
                result.add(limitReachedNotificationMapper.map(limitReached));
        }

        return result;
    }

}
