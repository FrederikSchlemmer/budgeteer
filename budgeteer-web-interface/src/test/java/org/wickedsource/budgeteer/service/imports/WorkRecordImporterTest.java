package org.wickedsource.budgeteer.service.imports;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.wickedsource.budgeteer.importer.aproda.AprodaWorkRecordsImporter;
import org.wickedsource.budgeteer.imports.api.ImportException;
import org.wickedsource.budgeteer.imports.api.ImportFile;
import org.wickedsource.budgeteer.imports.api.InvalidFileFormatException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkRecordImporterTest {

    @InjectMocks
    private ImportService importService;
    @Mock
    private ApplicationContext applicationContext;

    private void doImport() throws ImportException, InvalidFileFormatException {
        importService.doImport(1L, new AprodaWorkRecordsImporter(),
                Collections.singletonList(new ImportFile("file1", getClass().getResourceAsStream("testReport3.xlsx"))));
    }

    @Test
    void testGetSkippedRecordsNoSkippedRecords() throws Exception {
        WorkRecordDatabaseImporter workRecordDatabaseImporter = mock(WorkRecordDatabaseImporter.class, withSettings().useConstructor(1L, "Aproda Working Hours Importer"));
        when(applicationContext.getBean(eq(WorkRecordDatabaseImporter.class), eq(1L), any()))
                .thenReturn(workRecordDatabaseImporter);
        when(workRecordDatabaseImporter.getSkippedRecords()).thenReturn(new ArrayList<>());

        doImport();

        List<List<String>> skippedRecords = importService.getSkippedRecords();

        Assertions.assertThat(skippedRecords)
                .hasSize(3);
    }

    @Test
    void testGetSkippedRecordsSomeSkippedRecords() throws Exception {
        WorkRecordDatabaseImporter workRecordDatabaseImporter = mock(WorkRecordDatabaseImporter.class, withSettings().useConstructor(1L, "Aproda Working Hours Importer"));
        when(applicationContext.getBean(eq(WorkRecordDatabaseImporter.class), eq(1L), any()))
                .thenReturn(workRecordDatabaseImporter);
        when(workRecordDatabaseImporter.getSkippedRecords()).thenReturn(Arrays.asList(
                Arrays.asList("Fall, Klara", "Budget1", "540", "06.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                Arrays.asList("Fall, Klara", "Budget1", "420", "08.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                Arrays.asList("Fall, Klara", "Budget2", "420", "13.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                Arrays.asList("Fall, Klara", "Budget1", "480", "28.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                Arrays.asList("Fall, Klara", "Budget2", "420", "13.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                Arrays.asList("Fall, Klara", "Budget1", "480", "28.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                Arrays.asList("Fall, Klara", "Budget1", "510", "29.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                Arrays.asList("Fall, Klara", "Budget2", "510", "30.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                Arrays.asList("Fall, Klara", "Budget2", "480", "31.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range")
        ));

        doImport();

        List<List<String>> skippedRecords = importService.getSkippedRecords();

        Assertions.assertThat(skippedRecords)
                .hasSize(12)
                .containsAll(Arrays.asList(
                        Arrays.asList("Fall, Klara", "Budget1", "540", "06.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                        Arrays.asList("Fall, Klara", "Budget1", "420", "08.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                        Arrays.asList("Fall, Klara", "Budget2", "420", "13.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                        Arrays.asList("Fall, Klara", "Budget1", "480", "28.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                        Arrays.asList("Fall, Klara", "Budget2", "420", "13.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                        Arrays.asList("Fall, Klara", "Budget1", "480", "28.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                        Arrays.asList("Fall, Klara", "Budget1", "510", "29.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                        Arrays.asList("Fall, Klara", "Budget2", "510", "30.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range"),
                        Arrays.asList("Fall, Klara", "Budget2", "480", "31.10.14 00:00", "EUR 0.00", "", "Record is out of project-date-range")
                ));
    }
}
