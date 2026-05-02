package com.triplify.domain.error;

public sealed interface StatisticError extends DomainError permits StatisticError.NotFound {

    record NotFound(String statisticId) implements StatisticError {
        @Override
        public String code() {
            return "error.statistic.not.found";
        }

        @Override
        public String message() {
            return "Statistic '%s' not found".formatted(statisticId);
        }
    }
}
