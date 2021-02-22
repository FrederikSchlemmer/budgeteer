package org.wickedsource.budgeteer.service.person;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.person.PersonBaseDataBean;
import org.wickedsource.budgeteer.persistence.person.PersonDetailDataBean;
import org.wickedsource.budgeteer.persistence.person.PersonRepository;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {
    @InjectMocks
    private PersonService personService;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private PersonBaseDataMapper personBaseDataMapper;
    @Mock
    private PersonDetailDataMapper personDetailDataMapper;

    private final Date fixedDate = new Date();

    @Test
    void testLoadPeopleBaseData() {
        when(personRepository.findBaseDataByProjectId(1L)).thenReturn(Collections.singletonList(createPersonBaseDataBean()));
        when(personBaseDataMapper.map(anyList())).thenCallRealMethod();
        when(personBaseDataMapper.map(any(PersonBaseDataBean.class))).thenCallRealMethod();

        List<PersonBaseData> data = personService.loadPeopleBaseData(1L);

        Assertions.assertThat(data)
                .hasSize(1)
                .containsExactly(new PersonBaseData()
                        .setId(1L)
                        .setName("person1")
                        .setAverageDailyRate(MoneyUtil.createMoneyFromCents(10000L))
                        .setLastBooked(fixedDate)
                        .setDefaultDailyRate(MoneyUtil.createMoney(10)));
    }


    @Test
    void testLoadPersonDetailData() {
        when(personRepository.findDetailDataByPersonId(1L)).thenReturn(createPersonDetailDataBean());
        when(personDetailDataMapper.map(any(PersonDetailDataBean.class))).thenCallRealMethod();

        PersonDetailData data = personService.loadPersonDetailData(1L);

        Assertions.assertThat(data)
                .isEqualTo(new PersonDetailData()
                .setName("person1")
                .setAverageDailyRate(MoneyUtil.createMoneyFromCents(123456L))
                .setLastBookedDate(fixedDate)
                .setFirstBookedDate(fixedDate)
                .setBudgetBurned(MoneyUtil.createMoneyFromCents(654321L))
                .setHoursBooked(5.0d));
    }


    @Test
    void testLoadPersonBaseData() {
        when(personRepository.findBaseDataByPersonId(1L)).thenReturn(createPersonBaseDataBean());
        when(personBaseDataMapper.map(any(PersonBaseDataBean.class))).thenCallRealMethod();

        PersonBaseData bean = personService.loadPersonBaseData(1L);

        Assertions.assertThat(bean)
                .isEqualTo(new PersonBaseData()
                        .setId(1L)
                        .setName("person1")
                        .setAverageDailyRate(MoneyUtil.createMoneyFromCents(10000L))
                        .setLastBooked(fixedDate)
                        .setDefaultDailyRate(MoneyUtil.createMoney(10)));
    }

    private PersonBaseDataBean createPersonBaseDataBean() {
        return new PersonBaseDataBean(1L, "person1", 500000000L, 50000L, fixedDate, MoneyUtil.createMoney(10));
    }

    private PersonDetailDataBean createPersonDetailDataBean() {
        return new PersonDetailDataBean(1L, "person1", 6172800000L, 50000L, fixedDate, fixedDate, 5.0d, 654321L);
    }
}
