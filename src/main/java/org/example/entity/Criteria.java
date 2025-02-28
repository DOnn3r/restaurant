package org.example.entity;

import java.time.LocalDateTime;

public class Criteria {
    private String field;
    private Object value;
    private Double minValue;
    private Double maxValue;
    private LocalDateTime minDate;
    private LocalDateTime maxDate;

    public Criteria(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    public Criteria(String field, Double minValue, Double maxValue) {
        this.field = field;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public Criteria(String field, LocalDateTime minDate, LocalDateTime maxDate) {
        this.field = field;
        this.minDate = minDate;
        this.maxDate = maxDate;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public Double getMinValue() {
        return minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public LocalDateTime getMinDate() {
        return minDate;
    }

    public LocalDateTime getMaxDate() {
        return maxDate;
    }
}

