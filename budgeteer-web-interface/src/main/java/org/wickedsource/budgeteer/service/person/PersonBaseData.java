package org.wickedsource.budgeteer.service.person;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.joda.money.Money;

import java.io.Serializable;
import java.util.Date;

@NoArgsConstructor
@Data
@Accessors(chain = true)
public class PersonBaseData implements Serializable {

    private Long id;
    private String name;
    private Money averageDailyRate;
    private Date lastBooked;
    private Money defaultDailyRate;

    public PersonBaseData(Long id) {
        this.id = id;
    }
}
