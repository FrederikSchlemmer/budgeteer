package org.wickedsource.budgeteer.service.imports;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ImporterRegistryTest {

    @Test
    void testRegistry() {
        ImporterRegistry registry = new ImporterRegistry();
        Assertions.assertThat(registry.getWorkingRecordsImporters())
                .hasSize(2);
    }
}
