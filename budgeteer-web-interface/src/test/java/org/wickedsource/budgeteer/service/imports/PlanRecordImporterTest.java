package org.wickedsource.budgeteer.service.imports;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.wickedsource.budgeteer.importer.resourceplan.ResourcePlanImporter;
import org.wickedsource.budgeteer.imports.api.ImportException;
import org.wickedsource.budgeteer.imports.api.ImportFile;
import org.wickedsource.budgeteer.imports.api.InvalidFileFormatException;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanRecordImporterTest {

    @InjectMocks
    private ImportService importService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ApplicationContext applicationContext;

    private void doImport() throws ImportException, InvalidFileFormatException {
        importService.doImport(1L, new ResourcePlanImporter(), Collections.singletonList(
                new ImportFile("resource_plan2.xlsx", getClass().getResourceAsStream("resource_plan2.xlsx"))));
    }

    @Test
    void testGetSkippedRecordsNoSkippedRecords() throws Exception {
        PlanRecordDatabaseImporter planRecordDatabaseImporter = mock(PlanRecordDatabaseImporter.class, withSettings().useConstructor(1L, "Resource Plan Importer"));
        when(applicationContext.getBean(eq(PlanRecordDatabaseImporter.class), eq(1L), any()))
                .thenReturn(planRecordDatabaseImporter);
        when(planRecordDatabaseImporter.getSkippedRecords()).thenReturn(new ArrayList<>());

        doImport();

        List<List<String>> skippedRecords = importService.getSkippedRecords();

        Assertions.assertThat(skippedRecords)
                .isEmpty();
    }

    @Test
    void testGetSkippedRecordsSomeSkippedRecords() throws Exception {
        PlanRecordDatabaseImporter planRecordDatabaseImporter = mock(PlanRecordDatabaseImporter.class, withSettings().useConstructor(1L, "Resource Plan Importer"));
        when(applicationContext.getBean(eq(PlanRecordDatabaseImporter.class), eq(1L), any()))
                .thenReturn(planRecordDatabaseImporter);
        when(planRecordDatabaseImporter.getSkippedRecords()).thenReturn(Arrays.asList(
                Arrays.asList("resource_plan2.xlsx", "01.01.14 00:00", "Pfahl, Martha", "Budget2", "480", "EUR 1000.00", "Record is out of project-date-range"),
                Arrays.asList("resource_plan2.xlsx", "13.01.14 00:00", "Pfahl, Martha", "Budget2", "480", "EUR 1000.00", "Record is out of project-date-range"),
                Arrays.asList("resource_plan2.xlsx", "14.01.14 00:00", "Pfahl, Martha", "Budget2", "480", "EUR 1000.00", "Record is out of project-date-range")));


        doImport();

        List<List<String>> skippedRecords = importService.getSkippedRecords();

        Assertions.assertThat(skippedRecords)
                .hasSize(3)
                .containsExactly(
                        Arrays.asList("resource_plan2.xlsx", "01.01.14 00:00", "Pfahl, Martha", "Budget2", "480", "EUR 1000.00", "Record is out of project-date-range"),
                        Arrays.asList("resource_plan2.xlsx", "13.01.14 00:00", "Pfahl, Martha", "Budget2", "480", "EUR 1000.00", "Record is out of project-date-range"),
                        Arrays.asList("resource_plan2.xlsx", "14.01.14 00:00", "Pfahl, Martha", "Budget2", "480", "EUR 1000.00", "Record is out of project-date-range"));
    }
}
