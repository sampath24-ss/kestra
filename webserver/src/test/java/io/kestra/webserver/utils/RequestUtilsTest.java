package io.kestra.webserver.utils;

import io.kestra.core.models.QueryFilter;
import io.kestra.core.models.flows.FlowScope;
import io.kestra.core.models.flows.State;
import io.kestra.core.repositories.ExecutionRepositoryInterface;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class RequestUtilsTest {

    @Test
    void toMap() {
        final Map<String, String> resultMap = RequestUtils.toMap(List.of("timestamp:2023-12-18T14:32:14Z"));

        assertThat(resultMap.get("timestamp"), is("2023-12-18T14:32:14Z"));
    }


    @Test
    void testMapLegacyParamsToFilters() {
        ZonedDateTime startDate = ZonedDateTime.parse("2024-01-01T10:00:00Z");
        ZonedDateTime endDate = ZonedDateTime.parse("2024-01-02T10:00:00Z");
        Duration timeRange = Duration.ofHours(24);
        List<State.Type> state = List.of(State.Type.RUNNING, State.Type.FAILED);

        List<QueryFilter> filters = RequestUtils.mapLegacyParamsToFilters(
            "test-query",
            "test-namespace",
            "test-flow",
            "test-trigger",
            null,
            startDate,
            endDate,
            null,
            List.of("key:value"),
            timeRange,
            ExecutionRepositoryInterface.ChildFilter.MAIN,
            state,
            "worker-1"
        );

        assertTrue(filters.stream().anyMatch(f -> f.field() == QueryFilter.Field.QUERY && f.value().equals("test-query")));
        assertTrue(filters.stream().anyMatch(f -> f.field() == QueryFilter.Field.NAMESPACE && f.value().equals("test-namespace")));
        assertTrue(filters.stream().anyMatch(f -> f.field() == QueryFilter.Field.FLOW_ID && f.value().equals("test-flow")));
        assertTrue(filters.stream().anyMatch(f -> f.field() == QueryFilter.Field.TRIGGER_ID && f.value().equals("test-trigger")));
        assertTrue(filters.stream().anyMatch(f -> f.field() == QueryFilter.Field.START_DATE && f.value().equals(startDate.toString())));
        assertTrue(filters.stream().anyMatch(f -> f.field() == QueryFilter.Field.END_DATE && f.value().equals(endDate.toString())));
        assertTrue(filters.stream().anyMatch(f -> f.field() == QueryFilter.Field.TIME_RANGE && f.value().equals(timeRange)));
        assertTrue(filters.stream().anyMatch(f -> f.field() == QueryFilter.Field.STATE && f.value().equals(state)));
    }

    @Test
    void testMapLegacyParamsToFiltersHandlesNulls() {
        List<QueryFilter> filters = RequestUtils.mapLegacyParamsToFilters(
            null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        assertTrue(filters.isEmpty(), "Filters should be empty when all inputs are null.");
    }

    @Test
    void testToFlowScopesValid() {
        List<FlowScope> result = RequestUtils.toFlowScopes(List.of("USER,SYSTEM"));

        assertEquals(2, result.size());
        assertTrue(result.contains(FlowScope.USER));
        assertTrue(result.contains(FlowScope.SYSTEM));
    }

    @Test
    void testToFlowScopesInvalidValue() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            RequestUtils.toFlowScopes(List.of("INVALID_SCOPE"))
        );

        assertTrue(exception.getMessage().contains("Invalid FlowScope value"));
    }

    @Test
    void testResolveAbsoluteDateTimeWithTimeRange() {
        ZonedDateTime now = ZonedDateTime.parse("2024-01-10T10:00:00Z");
        Duration timeRange = Duration.ofDays(7);
        ZonedDateTime resolved = RequestUtils.resolveAbsoluteDateTime(null, timeRange, now);

        assertEquals(now.minus(timeRange), resolved);
    }

    @Test
    void testResolveAbsoluteDateTimeWithAbsoluteDate() {
        ZonedDateTime fixedDate = ZonedDateTime.parse("2024-01-01T10:00:00Z");
        ZonedDateTime result = RequestUtils.resolveAbsoluteDateTime(fixedDate, null, ZonedDateTime.now());

        assertEquals(fixedDate, result);
    }

}