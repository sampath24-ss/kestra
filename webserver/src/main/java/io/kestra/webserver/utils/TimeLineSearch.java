package io.kestra.webserver.utils;

import io.kestra.core.models.QueryFilter;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
@Data
@Builder
public class TimeLineSearch {
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private Duration timeRange;
    public static TimeLineSearch extractFrom(List<QueryFilter> filters) {
        ZonedDateTime startDate = null;
        ZonedDateTime endDate = null;
        Duration timeRange = null;

        for (QueryFilter filter : filters) {
            switch (filter.field()) {
                case START_DATE -> startDate = ZonedDateTime.parse(filter.value().toString());
                case END_DATE -> endDate = ZonedDateTime.parse(filter.value().toString());
                case TIME_RANGE -> timeRange = parseDuration(filter.value().toString());
            }
        }

        return new TimeLineSearch(startDate, endDate, timeRange);
    }
    private static Duration parseDuration(String duration) {
        try {
           return Duration.parse(duration);
        } catch (DateTimeParseException e){
            throw new IllegalArgumentException("Invalid duration: " + duration);
        }
    }


}