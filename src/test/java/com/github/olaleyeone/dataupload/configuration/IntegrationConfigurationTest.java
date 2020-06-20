package com.github.olaleyeone.dataupload.configuration;

import com.github.olaleyeone.dataupload.test.component.ComponentTest;
import com.google.gson.Gson;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.github.olaleyeone.configuration.JacksonConfiguration.DEFAULT_DATE_TIME_FORMAT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IntegrationConfigurationTest extends ComponentTest {

    private Gson gson;

    @BeforeEach
    public void setUp() {
        gson = new IntegrationConfiguration().gson();
    }

    @Test
    void readOffsetDateTime() {
        OffsetDateTime now = OffsetDateTime.now();
        String format = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OffsetDateTime offsetDateTime = gson.fromJson(String.format("\"%s\"", format), OffsetDateTime.class);
        assertNotNull(offsetDateTime);
        assertEquals(now, offsetDateTime);
    }

    @Test
    void writeOffsetDateTime() {
        OffsetDateTime now = OffsetDateTime.now();
        String format = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        assertEquals(String.format("\"%s\"", format), gson.toJson(now));
    }

    @Test
    void readLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String format = now.format(DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime localDateTime = gson.fromJson(String.format("\"%s\"", format), LocalDateTime.class);
        assertNotNull(localDateTime);
        assertEquals(now, localDateTime);
    }

    @Test
    void writeLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String format = now.format(DateTimeFormatter.ISO_DATE_TIME);
        assertEquals(String.format("\"%s\"", format), gson.toJson(now));
    }

    @Test
    void readLocalDate() {
        LocalDate now = LocalDate.now();
        String format = now.format(DateTimeFormatter.ISO_DATE);
        LocalDate localDate = gson.fromJson(String.format("\"%s\"", format), LocalDate.class);
        assertNotNull(localDate);
        assertEquals(now, localDate);
    }

    @Test
    void writeLocalDate() {
        LocalDate now = LocalDate.now();
        String format = now.format(DateTimeFormatter.ISO_DATE);
        assertEquals(String.format("\"%s\"", format), gson.toJson(now));
    }

    @Test
    void writeDate() {
        Date now = new Date();
        String format = gson.toJson(now);
        assertEquals(String.format("\"%s\"", DateFormatUtils.format(now, DEFAULT_DATE_TIME_FORMAT)), format);
    }
}