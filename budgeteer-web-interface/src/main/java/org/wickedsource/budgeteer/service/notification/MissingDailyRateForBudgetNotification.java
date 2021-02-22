package org.wickedsource.budgeteer.service.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MissingDailyRateForBudgetNotification extends Notification {

    private long personId;
    private Date startDate;
    private Date endDate;
    private String personName;
    private String budgetName;
}
