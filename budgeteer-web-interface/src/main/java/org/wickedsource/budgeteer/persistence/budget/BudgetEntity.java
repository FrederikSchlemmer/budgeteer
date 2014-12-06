package org.wickedsource.budgeteer.persistence.budget;

import org.joda.money.Money;
import org.wickedsource.budgeteer.persistence.person.DailyRateEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.record.PlanRecordEntity;
import org.wickedsource.budgeteer.persistence.record.WorkRecordEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BUDGET",
        uniqueConstraints = {
                @UniqueConstraint(name = "UNIQUE_IMPORT_KEY_PER_PROJECT", columnNames = {"PROJECT_ID", "IMPORT_KEY"})
        })
public class BudgetEntity {

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String name;

    private Money total;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PROJECT_ID")
    private ProjectEntity project;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "budget")
    private List<BudgetTagEntity> tags = new ArrayList<BudgetTagEntity>();

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WorkRecordEntity> workRecords = new ArrayList<WorkRecordEntity>();

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PlanRecordEntity> planRecords = new ArrayList<PlanRecordEntity>();

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DailyRateEntity> dailyRates = new ArrayList<DailyRateEntity>();

    @Column(nullable = false, name = "IMPORT_KEY")
    private String importKey;

    public List<WorkRecordEntity> getWorkRecords() {
        return workRecords;
    }

    public void setWorkRecords(List<WorkRecordEntity> workRecords) {
        this.workRecords = workRecords;
    }

    public List<DailyRateEntity> getDailyRates() {
        return dailyRates;
    }

    public void setDailyRates(List<DailyRateEntity> dailyRates) {
        this.dailyRates = dailyRates;
    }

    public String getImportKey() {
        return importKey;
    }

    public void setImportKey(String importKey) {
        this.importKey = importKey;
    }

    public List<BudgetTagEntity> getTags() {
        return tags;
    }

    public void setTags(List<BudgetTagEntity> tags) {
        this.tags = tags;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Money getTotal() {
        return total;
    }

    public void setTotal(Money total) {
        this.total = total;
    }

    public List<PlanRecordEntity> getPlanRecords() {
        return planRecords;
    }

    public void setPlanRecords(List<PlanRecordEntity> planRecords) {
        this.planRecords = planRecords;
    }
}
