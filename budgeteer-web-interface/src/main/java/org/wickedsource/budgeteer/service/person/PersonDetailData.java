package org.wickedsource.budgeteer.service.person;

import lombok.Data;
import lombok.experimental.Accessors;
import org.joda.money.Money;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.util.Date;

@Data
@Accessors(chain = true)
public class PersonDetailData {

    private String name;
    private Money averageDailyRate;
    private Date firstBookedDate;
    private Date lastBookedDate;
    private Double hoursBooked;
    private Money budgetBurned;

}
