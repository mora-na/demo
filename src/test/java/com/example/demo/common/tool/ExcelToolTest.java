package com.example.demo.common.tool;

import com.example.demo.common.annotation.Excel;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelToolTest {

    @Test
    void exportAndImport_roundTrip() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 30, 0);
        SampleRow row = new SampleRow(1L, "Alice", "1", now);
        ByteArrayOutputStream outputStream = ExcelTool.exportToStream(Collections.singletonList(row), SampleRow.class);

        List<SampleRow> rows = ExcelTool.importFromStream(new ByteArrayInputStream(outputStream.toByteArray()), SampleRow.class, 0);
        assertEquals(1, rows.size());
        SampleRow imported = rows.get(0);
        assertNull(imported.id);
        assertEquals("Alice", imported.name);
        assertEquals("1", imported.sex);
        assertEquals(now, imported.createdAt);
    }

    @Test
    void exportToStream_emptyListStillCreatesWorkbook() {
        ByteArrayOutputStream outputStream = ExcelTool.exportToStream(Collections.emptyList(), SampleRow.class);
        assertNotNull(outputStream);
        assertNotEquals(0, outputStream.toByteArray().length);
    }

    private static class SampleRow {

        private Long id;

        @Excel("Name")
        private String name;

        @Excel(header = "Sex", mapping = {"0:Female", "1:Male"})
        private String sex;

        @Excel(header = "CreatedAt")
        private LocalDateTime createdAt;

        public SampleRow() {
        }

        public SampleRow(Long id, String name, String sex, LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.sex = sex;
            this.createdAt = createdAt;
        }
    }
}
