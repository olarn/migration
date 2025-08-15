package com.ttb.crm.service.migrationdata.service.specification;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class StgCaseInProgressSpecification {

    public static Specification<StgCaseInProgressModel> filterRecordStatus(String recordStatus) {
        return (root, query, cb) -> {
            query.distinct(true);

            if (ObjectUtils.isEmpty(recordStatus)) {
                return null;
            }

            return root.get("recordStatus").in(recordStatus);
        };
    }

    public static Specification<StgCaseInProgressModel> filterContactPersonEmailC(String contactPersonEmail) {
        return (root, query, cb) -> {
            if (StringUtils.isBlank(contactPersonEmail)) {
                return null;
            }
            return cb.like(cb.lower(root.get("contactPersonEmailC")), "%" + contactPersonEmail.toLowerCase() + "%");
        };
    }

    public static Specification<StgCaseInProgressModel> filterMigrationLot(List<String> migrationLots) {
        return (root, query, cb) -> {
            if (migrationLots.isEmpty()) {
                return null;
            }
            return root.get("migrationLot").in(migrationLots);
        };
    }

    public static Specification<StgCaseInProgressModel> filterLoadStatus(List<String> loadStatusList) {
        return (root, query, cb) -> {
            if (ObjectUtils.isEmpty(loadStatusList)) {
                return null;
            }

            List<String> nonNullStatuses = loadStatusList.stream()
                    .filter(s -> s != null && !s.trim().isEmpty())
                    .toList();

            boolean includeNull = loadStatusList.stream()
                    .anyMatch(s -> s == null || s.trim().isEmpty());

            Predicate predicate = null;

            if (!nonNullStatuses.isEmpty()) {
                predicate = root.get("loadStatus").in(nonNullStatuses);
            }

            if (includeNull) {
                Predicate isNullPredicate = cb.isNull(root.get("loadStatus"));
                predicate = (predicate == null)
                        ? isNullPredicate
                        : cb.or(predicate, isNullPredicate);
            }

            return predicate;
        };
    }

    public static Specification<StgCaseInProgressModel> filterStatusCode(List<String> statusList) {
        return (root, query, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(statusList)) {
                return null;
            }

            return root.get("statusCode").in(statusList);
        };
    }

    @SafeVarargs
    public static Specification<StgCaseInProgressModel> combineSpecifications(Specification<StgCaseInProgressModel>... specs) {
        return Stream.of(specs)
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(Specification.where(null));
    }
}
