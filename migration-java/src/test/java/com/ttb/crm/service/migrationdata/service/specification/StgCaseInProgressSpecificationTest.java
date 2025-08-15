package com.ttb.crm.service.migrationdata.service.specification;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class StgCaseInProgressSpecificationTest {

    private Root<StgCaseInProgressModel> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;

    @BeforeEach
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);
    }

    @Test
    void testFilterRecordStatus_WithNull_ShouldReturnNull() {
        Specification<StgCaseInProgressModel> spec = StgCaseInProgressSpecification.filterRecordStatus(null);
        assertNull(spec.toPredicate(root, query, cb));
    }

    @Test
    void testFilterContactPersonEmailC_Blank_ShouldReturnNull() {
        Specification<StgCaseInProgressModel> spec = StgCaseInProgressSpecification.filterContactPersonEmailC(" ");
        assertNull(spec.toPredicate(root, query, cb));
    }

    @Test
    void testFilterMigrationLot_EmptyList_ShouldReturnNull() {
        Specification<StgCaseInProgressModel> spec = StgCaseInProgressSpecification.filterMigrationLot(List.of());
        assertNull(spec.toPredicate(root, query, cb));
    }

    @Test
    void testFilterLoadStatus_WithNullAndEmpty_ShouldReturnNull() {
        Specification<StgCaseInProgressModel> spec = StgCaseInProgressSpecification.filterLoadStatus(null);
        assertNull(spec.toPredicate(root, query, cb));
    }

    @Test
    void testFilterStatusCode_WithEmpty_ShouldReturnNull() {
        Specification<StgCaseInProgressModel> spec = StgCaseInProgressSpecification.filterStatusCode(List.of());
        assertNull(spec.toPredicate(root, query, cb));
    }

    @Test
    void testCombineSpecifications_WithNulls_ShouldReturnNullSpec() {
        Specification<StgCaseInProgressModel> spec = StgCaseInProgressSpecification.combineSpecifications(null, null);
        assertNull(spec.toPredicate(root, query, cb));
    }
}
