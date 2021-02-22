package org.wickedsource.budgeteer.service.imports;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wickedsource.budgeteer.persistence.imports.ImportEntity;
import org.wickedsource.budgeteer.persistence.imports.ImportRepository;
import org.wickedsource.budgeteer.persistence.record.PlanRecordRepository;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @InjectMocks
    private ImportService importService;
    @Mock
    private ImportRepository importRepository;
    @Mock
    private WorkRecordRepository workRecordRepository;
    @Mock
    private PlanRecordRepository planRecordRepository;

    @Test
    void testLoadImports() {
        when(importRepository.findByProjectId(1L)).thenReturn(Collections.singletonList(createImportEntity()));

        List<Import> imports = importService.loadImports(1L);

        Assertions.assertThat(imports)
                .hasSize(1)
                .extracting(Import::getImportType)
                .containsExactly("TestImport");
    }

    @Test
    void testDeleteImport() {
        importService.deleteImport(1L);

        verify(importRepository, times(1)).deleteById(1L);
        verify(workRecordRepository, times(1)).deleteByImport(1L);
        verify(planRecordRepository, times(1)).deleteByImport(1L);
    }

    private ImportEntity createImportEntity() {
        ImportEntity entity = new ImportEntity();
        entity.setEndDate(new Date());
        entity.setStartDate(new Date());
        entity.setImportType("TestImport");
        entity.setId(1L);
        return entity;
    }

}
