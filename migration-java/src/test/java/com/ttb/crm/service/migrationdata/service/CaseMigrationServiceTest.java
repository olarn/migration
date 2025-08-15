package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.bean.InProgressMigrationResult;
import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixSla;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CaseMigrationServiceTest {
    @InjectMocks
    private CaseMigrationService caseMigrationService;

    private static ServiceTypeMatrixSla createSla(int hopNumber, String responsibleBu) {
        ServiceTypeMatrixSla sla = new ServiceTypeMatrixSla();
        sla.setHopNumber(hopNumber);
        sla.setResponsibleBu(responsibleBu);
        return sla;
    }

    private static StgSlaPerOwnerModel createSf(String ownerTeamC, String startDateTimeC) {
        StgSlaPerOwnerModel sf = new StgSlaPerOwnerModel();
        sf.setOwnerTeamNew(ownerTeamC);
        sf.setStartDateTimeC(startDateTimeC);
        return sf;
    }

    @Nested
    class stmTypeFix {

        private ServiceTypeMatrixModel stmModel;

        @BeforeEach
        void setUp() {
            stmModel = new ServiceTypeMatrixModel();
            stmModel.setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.FIX);
        }

        @Nested
        class successfulCase {
            @Test
            @DisplayName("Should successfully migrate when STM has 3 hops and SalesForce activities match 2 of them")
            void shouldMigrateWhenSTMHasThreeHopsAndTwoMatchingSalesForceActivities() {
                List<ServiceTypeMatrixSla> slas = List.of(
                        createSla(1, "BU1"),
                        createSla(2, "BU2"),
                        createSla(3, "BU3")
                );

                stmModel.setServiceTypeMatrixSlas(slas);
                List<StgSlaPerOwnerModel> sfActivities = List.of(
                        createSf("BU2", "2023-01-02T10:00:00Z"),
                        createSf("BU3", "2023-01-03T10:00:00Z"),
                        createSf("BU4", "2023-01-04T10:00:00Z")

                );

                InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);

                assertTrue(validate.isCanMigrate());
                assertEquals(2, validate.matchedActivities().size());
                assertEquals("BU2", validate.matchedActivities().getFirst().getOwnerTeamNew());
                assertEquals("BU3", validate.matchedActivities().getLast().getOwnerTeamNew());
            }

            @Test
            @DisplayName("Should migrate when full sequence (BU2 → BU3 → BU4) is matched at the end")
            void shouldMigrate_whenFullSequenceMatchedAtEnd() {
                List<ServiceTypeMatrixSla> slas = List.of(
                        createSla(1, "BU1"),
                        createSla(2, "BU2"),
                        createSla(3, "BU3"),
                        createSla(4, "BU4")
                );

                stmModel.setServiceTypeMatrixSlas(slas);
                List<StgSlaPerOwnerModel> sfActivities = List.of(
                        createSf("BU1", "2023-01-02T10:00:00Z"),
                        createSf("BU2", "2023-01-02T11:00:00Z"),
                        createSf("BU3", "2023-01-02T12:00:00Z"),
                        createSf("BU4", "2023-01-02T13:00:00Z"),
                        createSf("BU3", "2023-01-02T14:00:00Z"),
                        createSf("BU2", "2023-01-02T15:00:00Z"),
                        createSf("BU3", "2023-01-02T16:00:00Z"),
                        createSf("BU4", "2023-01-02T17:00:00Z")
                );

                InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
                assertTrue(validate.isCanMigrate());
                assertEquals(3, validate.matchedActivities().size());
                assertEquals("BU2", validate.matchedActivities().getFirst().getOwnerTeamNew());
                assertEquals(sfActivities.get(5).getStartDateTimeC(), validate.matchedActivities().getFirst().getStartDateTimeC());
                assertEquals("BU3", validate.matchedActivities().get(1).getOwnerTeamNew());
                assertEquals(sfActivities.get(6).getStartDateTimeC(), validate.matchedActivities().get(1).getStartDateTimeC());
                assertEquals("BU4", validate.matchedActivities().getLast().getOwnerTeamNew());
                assertEquals(sfActivities.getLast().getStartDateTimeC(), validate.matchedActivities().getLast().getStartDateTimeC());
            }

            @Test
            @DisplayName("Should migrate when partial sequence (BU2 → BU3) is matched")
            void shouldMigrate_whenPartialSequenceMatched_2and3() {
                List<ServiceTypeMatrixSla> slas = List.of(
                        createSla(1, "BU1"),
                        createSla(2, "BU2"),
                        createSla(3, "BU3"),
                        createSla(4, "BU4")
                );

                stmModel.setServiceTypeMatrixSlas(slas);
                List<StgSlaPerOwnerModel> sfActivities = List.of(
                        createSf("BU1", "2023-01-02T10:00:00Z"),
                        createSf("BU2", "2023-01-02T11:00:00Z"),
                        createSf("BU3", "2023-01-02T12:00:00Z"),
                        createSf("BU5", "2023-01-02T13:00:00Z")
                );

                InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
                assertTrue(validate.isCanMigrate());
                assertEquals(2, validate.matchedActivities().size());
                assertEquals("BU2", validate.matchedActivities().getFirst().getOwnerTeamNew());
                assertEquals("BU3", validate.matchedActivities().get(1).getOwnerTeamNew());
            }

            @Test
            @DisplayName("Should migrate when only BU2 is matched")
            void shouldMigrate_whenOnlyHop2Matched() {
                List<ServiceTypeMatrixSla> slas = List.of(
                        createSla(1, "BU1"),
                        createSla(2, "BU2"),
                        createSla(3, "BU3")
                );

                stmModel.setServiceTypeMatrixSlas(slas);
                List<StgSlaPerOwnerModel> sfActivities = List.of(
                        createSf("BU1", "2023-01-02T10:00:00Z"),
                        createSf("BU5", "2023-01-02T11:00:00Z"),
                        createSf("BU2", "2023-01-02T12:00:00Z"),
                        createSf("BU6", "2023-01-02T13:00:00Z")
                );

                InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
                assertTrue(validate.isCanMigrate());
                assertEquals(1, validate.matchedActivities().size());
                assertEquals("BU2", validate.matchedActivities().getFirst().getOwnerTeamNew());
            }

            @Test
            @DisplayName("Should use last occurrence of BU2 to match subsequent hops")
            void shouldUseLastOccurrenceOfHop2AsStartingPoint() {
                List<ServiceTypeMatrixSla> slas = List.of(
                        createSla(1, "BU1"),
                        createSla(2, "BU2"),
                        createSla(3, "BU3")
                );

                stmModel.setServiceTypeMatrixSlas(slas);
                List<StgSlaPerOwnerModel> sfActivities = List.of(
                        createSf("BU2", "2023-01-02T10:00:00Z"),
                        createSf("BU3", "2023-01-02T11:00:00Z"),
                        createSf("BU2", "2023-01-02T12:00:00Z"),
                        createSf("BU3", "2023-01-02T13:00:00Z")
                );

                InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
                assertTrue(validate.isCanMigrate());
                assertEquals(2, validate.matchedActivities().size());
                assertEquals("BU2", validate.matchedActivities().getFirst().getOwnerTeamNew());
                assertEquals("BU3", validate.matchedActivities().getLast().getOwnerTeamNew());
                assertEquals(sfActivities.get(2).getStartDateTimeC(), validate.matchedActivities().getFirst().getStartDateTimeC());
                assertEquals(sfActivities.getLast().getStartDateTimeC(), validate.matchedActivities().getLast().getStartDateTimeC());
            }

            @Test
            @DisplayName("Should skip null/blank ownerTeamC and still migrate if pattern matches")
            void shouldHandleNullOrBlankOwnerTeamC() {
                List<ServiceTypeMatrixSla> slas = List.of(
                        createSla(1, "BU1"),
                        createSla(2, "BU2"),
                        createSla(3, "BU3")
                );

                stmModel.setServiceTypeMatrixSlas(slas);
                List<StgSlaPerOwnerModel> sfActivities = List.of(
                        createSf("BU1", "2023-01-02T10:00:00Z"),
                        createSf("", "2023-01-02T11:00:00Z"),
                        createSf(null, "2023-01-02T12:00:00Z"),
                        createSf("BU2", "2023-01-02T13:00:00Z"),
                        createSf("BU3", "2023-01-02T14:00:00Z")
                );

                InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
                assertTrue(validate.isCanMigrate());
                assertEquals(2, validate.matchedActivities().size());
                assertEquals("BU2", validate.matchedActivities().getFirst().getOwnerTeamNew());
                assertEquals("BU3", validate.matchedActivities().getLast().getOwnerTeamNew());
                assertEquals(sfActivities.get(3).getStartDateTimeC(), validate.matchedActivities().getFirst().getStartDateTimeC());
                assertEquals(sfActivities.getLast().getStartDateTimeC(), validate.matchedActivities().getLast().getStartDateTimeC());
            }

            @Test
            @DisplayName("Should migrate when partial sequence (BU2) is matched")
            void shouldMigrate_whenOnlyHop2OnlyMatched() {
                List<ServiceTypeMatrixSla> slas = List.of(
                        createSla(1, "BU1"),
                        createSla(2, "BU2"),
                        createSla(3, "BU3")
                );

                stmModel.setServiceTypeMatrixSlas(slas);
                List<StgSlaPerOwnerModel> sfActivities = List.of(
                        createSf("BU3", "2023-01-02T10:00:00Z"),
                        createSf("BU2", "2023-01-02T11:00:00Z"),
                        createSf("BU3", "2023-01-02T12:00:00Z"),
                        createSf("BU2", "2023-01-02T13:00:00Z")
                );

                InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
                assertTrue(validate.isCanMigrate());
                assertEquals(1, validate.matchedActivities().size());
                assertEquals("BU2", validate.matchedActivities().getFirst().getOwnerTeamNew());
                assertEquals(sfActivities.getLast().getStartDateTimeC(), validate.matchedActivities().getFirst().getStartDateTimeC());
            }
        }

        @Nested
        class failedCase {
            @Test
            @DisplayName("Should not migrate when BU2 is missing in SF activities")
            void shouldNotMigrate_whenNoMatchingHopAfterFirst() {
                List<ServiceTypeMatrixSla> slas = List.of(
                        createSla(1, "BU1"),
                        createSla(2, "BU2"),
                        createSla(3, "BU3")
                );

                stmModel.setServiceTypeMatrixSlas(slas);
                List<StgSlaPerOwnerModel> sfActivities = List.of(
                        createSf("BU1", "2023-01-02T10:00:00Z"),
                        createSf("BU5", "2023-01-02T11:00:00Z"),
                        createSf("BU4", "2023-01-02T12:00:00Z")
                );

                InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
                assertFalse(validate.isCanMigrate());
                assertEquals(0, validate.matchedActivities().size());
            }

            @Test
            @DisplayName("Should not migrate when STM has only one hop (skipped)")
            void shouldNotMigrate_whenNoHopAfterSkip() {
                List<ServiceTypeMatrixSla> slas = List.of(
                        createSla(1, "BU1")
                );

                stmModel.setServiceTypeMatrixSlas(slas);
                List<StgSlaPerOwnerModel> sfActivities = List.of(
                        createSf("BU1", "2023-01-02T10:00:00Z"),
                        createSf("BU2", "2023-01-02T11:00:00Z"),
                        createSf("BU3", "2023-01-02T12:00:00Z")
                );

                InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
                assertFalse(validate.isCanMigrate());
                assertEquals(0, validate.matchedActivities().size());
            }

            @Test
            @DisplayName("Should not migrate when SF activity list is empty")
            void shouldHandleEmptySfList() {
                List<ServiceTypeMatrixSla> slas = List.of(
                        createSla(1, "BU1"),
                        createSla(2, "BU2"),
                        createSla(3, "BU3")
                );

                stmModel.setServiceTypeMatrixSlas(slas);
                List<StgSlaPerOwnerModel> sfActivities = List.of();

                InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
                assertFalse(validate.isCanMigrate());
                assertEquals(0, validate.matchedActivities().size());
            }

            @Test
            @DisplayName("Should not migrate when BU2 does not exist in SF activities")
            void shouldNotMigrate_whenNoHop2InSf() {
                List<ServiceTypeMatrixSla> slas = List.of(
                        createSla(1, "BU1"),
                        createSla(2, "BU2"),
                        createSla(3, "BU3")
                );

                stmModel.setServiceTypeMatrixSlas(slas);
                List<StgSlaPerOwnerModel> sfActivities = List.of(
                        createSf("BU1", "2023-01-02T10:00:00Z"),
                        createSf("BU3", "2023-01-02T11:00:00Z"),
                        createSf("BU4", "2023-01-02T12:00:00Z")
                );

                InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
                assertFalse(validate.isCanMigrate());
                assertEquals(0, validate.matchedActivities().size());
            }
        }
    }

    @Nested
    class stmTypeDynamic {
        private ServiceTypeMatrixModel stmModel;

        @BeforeEach
        void setUp() {
            stmModel = new ServiceTypeMatrixModel();
            stmModel.setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.DYNAMIC);
        }

        @Test
        void shouldMigrateWhenSTMHop2MatchWithSfFirstHop() {
            List<ServiceTypeMatrixSla> slas = List.of(
                    createSla(1, "BU1"),
                    createSla(2, "BU2"),
                    createSla(3, "BU3")
            );

            stmModel.setServiceTypeMatrixSlas(slas);

            List<StgSlaPerOwnerModel> sfActivities = List.of(
                    createSf("BU2", "2023-01-02T10:00:00Z"),
                    createSf("BU3", "2023-01-02T11:00:00Z"),
                    createSf("BU4", "2023-01-02T12:00:00Z"),
                    createSf("BU1", "2023-01-02T13:00:00Z"),
                    createSf("BU5", "2023-01-02T14:00:00Z")
            );

            InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
            assertTrue(validate.isCanMigrate());
            assertEquals(5, validate.matchedActivities().size());
            assertEquals("BU2", validate.matchedActivities().getFirst().getOwnerTeamNew());
            assertEquals("BU3", validate.matchedActivities().get(1).getOwnerTeamNew());
            assertEquals("BU4", validate.matchedActivities().get(2).getOwnerTeamNew());
            assertEquals("BU1", validate.matchedActivities().get(3).getOwnerTeamNew());
            assertEquals("BU5", validate.matchedActivities().getLast().getOwnerTeamNew());
            assertEquals(sfActivities.getFirst().getStartDateTimeC(), validate.matchedActivities().getFirst().getStartDateTimeC());
            assertEquals(sfActivities.get(1).getStartDateTimeC(), validate.matchedActivities().get(1).getStartDateTimeC());
            assertEquals(sfActivities.get(2).getStartDateTimeC(), validate.matchedActivities().get(2).getStartDateTimeC());
            assertEquals(sfActivities.get(3).getStartDateTimeC(), validate.matchedActivities().get(3).getStartDateTimeC());
            assertEquals(sfActivities.getLast().getStartDateTimeC(), validate.matchedActivities().getLast().getStartDateTimeC());
        }

        @Test
        void shouldNotMigrateWhenSTMHop2NotMatchWithSfFirstHop() {
            List<ServiceTypeMatrixSla> slas = List.of(
                    createSla(1, "BU1"),
                    createSla(2, "BU2"),
                    createSla(3, "BU3")
            );

            stmModel.setServiceTypeMatrixSlas(slas);

            List<StgSlaPerOwnerModel> sfActivities = List.of(
                    createSf("BU3", "2023-01-02T10:00:00Z"),
                    createSf("BU2", "2023-01-02T11:00:00Z"),
                    createSf("BU4", "2023-01-02T12:00:00Z"),
                    createSf("BU1", "2023-01-02T13:00:00Z"),
                    createSf("BU5", "2023-01-02T14:00:00Z")
            );

            InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
            assertFalse(validate.isCanMigrate());
            assertEquals(0, validate.matchedActivities().size());
        }

        @Test
        void shouldNotMigrateWhenSfIsEmpty() {
            List<ServiceTypeMatrixSla> slas = List.of(
                    createSla(1, "BU1"),
                    createSla(2, "BU2"),
                    createSla(3, "BU3")
            );

            stmModel.setServiceTypeMatrixSlas(slas);

            List<StgSlaPerOwnerModel> sfActivities = List.of();

            InProgressMigrationResult validate = caseMigrationService.shouldUseLegacySlaData(stmModel, sfActivities);
            assertFalse(validate.isCanMigrate());
            assertEquals(0, validate.matchedActivities().size());
        }

    }

}
