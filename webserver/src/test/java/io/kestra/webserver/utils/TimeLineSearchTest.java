package io.kestra.webserver.utils;


import io.kestra.core.models.QueryFilter;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeLineSearchTest {

    @Test
    void testExtractFrom() {
        // GIVEN
        ZonedDateTime startDate = ZonedDateTime.parse("2024-01-01T10:00:00Z");
        ZonedDateTime endDate = ZonedDateTime.parse("2024-01-02T10:00:00Z");
        Duration timeRange = Duration.ofHours(24);

        List<QueryFilter> filters = List.of(
            QueryFilter.builder().field(QueryFilter.Field.START_DATE).operation(QueryFilter.Op.EQUALS).value(startDate.toString()).build(),
            QueryFilter.builder().field(QueryFilter.Field.END_DATE).operation(QueryFilter.Op.EQUALS).value(endDate.toString()).build(),
            QueryFilter.builder().field(QueryFilter.Field.TIME_RANGE).operation(QueryFilter.Op.EQUALS).value(timeRange.toString()).build()
        );
        // WHEN
        TimeLineSearch result = TimeLineSearch.extractFrom(filters);
        // THEN
        assertNotNull(result);
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        assertEquals(timeRange, result.getTimeRange());
    }

    @Test
    void testExtractFromWithInvalidDuration() {
        // GIVEN
        List<QueryFilter> filters = List.of(
            QueryFilter.builder().field(QueryFilter.Field.TIME_RANGE).operation(QueryFilter.Op.EQUALS).value("invalid-duration").build()
        );
        // WHEN
        Exception exception = assertThrows(IllegalArgumentException.class, () -> TimeLineSearch.extractFrom(filters));
        // THEN
        assertTrue(exception.getMessage().contains("Invalid duration"));
    }

    @Test
    void testUpdateFiltersRemovesTimeRange() {
        // GIVEN
        ZonedDateTime startDate = ZonedDateTime.parse("2024-01-01T10:00:00Z");
        ZonedDateTime newStartDate = ZonedDateTime.parse("2024-01-02T10:00:00Z");

        List<QueryFilter> filters = List.of(
            QueryFilter.builder().field(QueryFilter.Field.START_DATE).operation(QueryFilter.Op.EQUALS).value(startDate.toString()).build(),
            QueryFilter.builder().field(QueryFilter.Field.TIME_RANGE).operation(QueryFilter.Op.EQUALS).value(Duration.ofHours(24).toString()).build()
        );
        // WHEN
        List<QueryFilter> updatedFilters = QueryFilterUtils.updateFilters(filters, newStartDate);
        // THEN
        assertEquals(1, updatedFilters.size()); // TIME_RANGE filter should be removed
        assertEquals(QueryFilter.Field.START_DATE, updatedFilters.get(0).field());
        assertEquals(newStartDate.toString(), updatedFilters.get(0).value());
    }

    @Test
    void testUpdateFiltersKeepsUnrelatedFilters() {
        // GIVEN
        ZonedDateTime startDate = ZonedDateTime.parse("2024-01-01T10:00:00Z");
        ZonedDateTime newStartDate = ZonedDateTime.parse("2024-01-02T10:00:00Z");

        List<QueryFilter> filters = List.of(
            QueryFilter.builder().field(QueryFilter.Field.START_DATE).operation(QueryFilter.Op.EQUALS).value(startDate.toString()).build(),
            QueryFilter.builder().field(QueryFilter.Field.END_DATE).operation(QueryFilter.Op.EQUALS).value("2024-01-03T10:00:00Z").build(),
            QueryFilter.builder().field(QueryFilter.Field.TIME_RANGE).operation(QueryFilter.Op.EQUALS).value(Duration.ofHours(24).toString()).build()
        );
        // WHEN
        List<QueryFilter> updatedFilters = QueryFilterUtils.updateFilters(filters, newStartDate);
        // THEN
        assertEquals(2, updatedFilters.size()); // TIME_RANGE should be removed, others should stay
        assertTrue(updatedFilters.stream().anyMatch(f -> f.field() == QueryFilter.Field.START_DATE));
        assertTrue(updatedFilters.stream().anyMatch(f -> f.field() == QueryFilter.Field.END_DATE));
    }
}