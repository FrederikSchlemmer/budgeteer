package org.wickedsource.budgeteer.persistence.budget;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface BudgetRepository extends CrudRepository<BudgetEntity, Long> {

    boolean existsByImportKeyAndProjectId(String importKey, long projectId);
    boolean existsByNameAndProjectId(String name, long projectId);

    List<BudgetEntity> findByProjectId(long projectId);
    List<BudgetEntity> findByProjectIdOrderByNameAsc(long projectId);

    List<BudgetEntity> findByContractId(long contractId);

    void deleteByProjectId(long projectId);
}
