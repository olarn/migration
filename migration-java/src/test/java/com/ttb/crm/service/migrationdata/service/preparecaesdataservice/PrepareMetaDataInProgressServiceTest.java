package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;

import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
import com.ttb.crm.service.migrationdata.helper.MockSTM;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixSla;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.repository.masterManagement.HolidayRepository;
import com.ttb.crm.service.migrationdata.repository.masterManagement.ServiceTypeMatrixRepository;
import com.ttb.crm.service.migrationdata.service.SlaService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class PrepareMetaDataInProgressServiceTest {

    @Autowired
    private PrepareMetaDataInProgressService prepareMetaDataInProgressService;

    @Autowired
    private SlaService slaService;

    @Autowired
    private MockSTM mockSTM;

    @MockitoBean
    private ServiceTypeMatrixRepository stmRepository;

    @MockitoBean
    private HolidayRepository holidayRepository;

    @Nested
    class isClosedByCreator {
        @Test
        void whenStmIsCloseByCreator_thenTrue() {
            ServiceTypeMatrixModel stm = mockSTM.mockSTMClosedByCreator();

            boolean isClosedByCreator = prepareMetaDataInProgressService.isClosedByCreator(stm);

            assertTrue(isClosedByCreator);
        }

        @Test
        void whenStmWithEmptySla_thenFalse() {
            ServiceTypeMatrixModel stm = mockSTM.mockSTMClosedByCreator();
            stm.setServiceTypeMatrixSlas(Collections.emptyList());
            boolean isClosedByCreator = prepareMetaDataInProgressService.isClosedByCreator(stm);

            assertFalse(isClosedByCreator);
        }

        @Test
        void whenStmWithOneSla_thenFalse() {
            ServiceTypeMatrixModel stm = mockSTM.mockSTMClosedByCreator();
            stm.setServiceTypeMatrixSlas(List.of(stm.getServiceTypeMatrixSlas().getFirst()));
            boolean isClosedByCreator = prepareMetaDataInProgressService.isClosedByCreator(stm);

            assertFalse(isClosedByCreator);
        }

        @Test
        void whenStmIsNull_thenFalse() {
            boolean isClosedByCreator = prepareMetaDataInProgressService.isClosedByCreator(null);

            assertFalse(isClosedByCreator);
        }
    }

    @Nested
    class setHopEndClosedByCreator {
        @Test
        void whenStmIsCloseByCreator_thenHopEndSetResponsibleBuFirstHopInfo() {
            ServiceTypeMatrixModel stm = mockSTM.mockSTMClosedByCreator();

            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            BeanUtils.copyProperties(stm.getServiceTypeMatrixSlas().getFirst(), firstHop);
            firstHop.setTeamName("firstTeam")
                    .setTeamId(UUID.randomUUID());

            CaseSlaHopModel secondHop = new CaseSlaHopModel();
            BeanUtils.copyProperties(stm.getServiceTypeMatrixSlas().get(1), secondHop);

            CaseSlaHopModel thirdHop = new CaseSlaHopModel();
            BeanUtils.copyProperties(stm.getServiceTypeMatrixSlas().getLast(), thirdHop);

            List<CaseSlaHopModel> hopModels = new ArrayList<>(List.of(firstHop, secondHop, thirdHop));

            prepareMetaDataInProgressService.setHopEndClosedByCreator(stm, hopModels);

            assertEquals(firstHop.getTeamName(), hopModels.getLast().getTeamName());
            assertEquals(firstHop.getTeamId(), hopModels.getLast().getTeamId());
        }
    }

    @Nested
    class isSpecialStmCode {

        @Test
        void whenStmCodeOldIsSpecialAndFoundStmCodeNew_thenTrue() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setServiceTypeMatrixCodeOld("18050");
            dto.setServiceTypeMatrixCode("STM00001");
            ServiceTypeMatrixModel stm = new ServiceTypeMatrixModel();
            stm.setServiceTypeMatrixCode(dto.getServiceTypeMatrixCode())
                    .setServiceTypeMatrixId(UUID.randomUUID());
            when(stmRepository.findByServiceTypeMatrixCode(anyString())).thenReturn(stm);
            boolean isSpecialStmCode = prepareMetaDataInProgressService.isSpecialStmCode(dto);
            assertTrue(isSpecialStmCode);
        }

        @Test
        void whenStmCodeOldIsSpecialAndNotFoundStmCodeNew_thenTrue() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setServiceTypeMatrixCodeOld("18050");
            dto.setServiceTypeMatrixCode("STM00001");
            when(stmRepository.findByServiceTypeMatrixCode(anyString())).thenReturn(null);
            boolean isSpecialStmCode = prepareMetaDataInProgressService.isSpecialStmCode(dto);
            assertFalse(isSpecialStmCode);
        }

        @Test
        void whenStmCodeOldIsSpecialButCannotGetStmByStmCodeNew_thenFalse() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setServiceTypeMatrixCodeOld("18050");
            dto.setServiceTypeMatrixCode("STM00001");
            when(stmRepository.findByServiceTypeMatrixCode(anyString())).thenThrow(new RuntimeException("Cannot get STM by STM code"));
            boolean isSpecialStmCode = prepareMetaDataInProgressService.isSpecialStmCode(dto);
            assertFalse(isSpecialStmCode);
        }

        @Test
        void whenStmCodeOldIsSpecialButStmCodeNewIsBlank_thenFalse() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setServiceTypeMatrixCodeOld("18050");
            dto.setServiceTypeMatrixCode("");
            boolean isSpecialStmCode = prepareMetaDataInProgressService.isSpecialStmCode(dto);
            assertFalse(isSpecialStmCode);
        }

        @Test
        void whenStmCodeOldIsSpecialButStmCodeNewIsNull_thenFalse() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setServiceTypeMatrixCodeOld("18050");
            dto.setServiceTypeMatrixCode(null);
            boolean isSpecialStmCode = prepareMetaDataInProgressService.isSpecialStmCode(dto);
            assertFalse(isSpecialStmCode);
        }

        @Test
        void whenStmCodeOldIsNull_thenFalse() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setServiceTypeMatrixCodeOld(null);
            boolean isSpecialStmCode = prepareMetaDataInProgressService.isSpecialStmCode(dto);
            assertFalse(isSpecialStmCode);
        }

        @Test
        void whenDtoIsNull_thenFalse() {
            CreateCaseDTO dto = null;
            boolean isSpecialStmCode = prepareMetaDataInProgressService.isSpecialStmCode(dto);
            assertFalse(isSpecialStmCode);
        }
    }

    @Nested
    class isSpecialTeamName {
        @Test
        void whenBuNameOldIsSpecial_thenTrue() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setResponsibleBuOld("TMB Contact Center-Follow up");
            boolean isSpecialTeamName = prepareMetaDataInProgressService.isSpecialTeamName(dto);
            assertTrue(isSpecialTeamName);
        }

        @Test
        void whenBuNameOldIsNull_thenFalse() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setResponsibleBuOld(null);
            boolean isSpecialTeamName = prepareMetaDataInProgressService.isSpecialTeamName(dto);
            assertFalse(isSpecialTeamName);
        }

        @Test
        void whenBuNameOldIsBlank_thenFalse() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setResponsibleBuOld(" ");
            boolean isSpecialTeamName = prepareMetaDataInProgressService.isSpecialTeamName(dto);
            assertFalse(isSpecialTeamName);
        }

        @Test
        void whenDtoIsNull_thenFalse() {
            CreateCaseDTO dto = null;
            boolean isSpecialStmCode = prepareMetaDataInProgressService.isSpecialTeamName(dto);
            assertFalse(isSpecialStmCode);
        }
    }

    @Nested
    class isAutoCreateCase {

        @Test
        void whenOwnerTeamNameAndCreateByTeamNameAreSameAndLogIsEmpty_thenTrue() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setOwnerTeamName("Team1");
            dto.setCreateByTeamName("Team1");
            List<StgSlaPerOwnerModel> slaLogs = new ArrayList<>();
            boolean isAutoCreateCase = prepareMetaDataInProgressService.isAutoCreateCase(dto, slaLogs);
            assertTrue(isAutoCreateCase);
        }

        @Test
        void whenOwnerTeamNameAndCreateByTeamNameAreSameAndLogHaveSameTeam_thenTrue() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setOwnerTeamName("Team1");
            dto.setCreateByTeamName("Team1");
            com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel slaLog = new StgSlaPerOwnerModel();
            slaLog.setOwnerTeamC("Team1")
                    .setOwnerTeamNew("Team1");
            List<StgSlaPerOwnerModel> slaLogs = new ArrayList<>(List.of(slaLog));
            boolean isAutoCreateCase = prepareMetaDataInProgressService.isAutoCreateCase(dto, slaLogs);
            assertTrue(isAutoCreateCase);
        }

        @Test
        void whenDtoIsNull_thenFalse() {
            CreateCaseDTO dto = null;
            List<StgSlaPerOwnerModel> slaLogs = new ArrayList<>();
            boolean isAutoCreateCase = prepareMetaDataInProgressService.isAutoCreateCase(dto, slaLogs);
            assertFalse(isAutoCreateCase);
        }


        @Test
        void whenCreateByTeamNameIsNull_thenFalse() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setCreateByTeamName(null);
            List<StgSlaPerOwnerModel> slaLogs = new ArrayList<>();
            boolean isAutoCreateCase = prepareMetaDataInProgressService.isAutoCreateCase(dto, slaLogs);
            assertFalse(isAutoCreateCase);
        }

        @Test
        void whenCreateByTeamNameIsBlank_thenFalse() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setCreateByTeamName(" ");
            List<StgSlaPerOwnerModel> slaLogs = new ArrayList<>();
            boolean isAutoCreateCase = prepareMetaDataInProgressService.isAutoCreateCase(dto, slaLogs);
            assertFalse(isAutoCreateCase);
        }

        @Test
        void whenOwnerTeamNameIsNull_thenFalse() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setCreateByTeamName("CreateTeam");
            dto.setOwnerTeamName(null);
            List<StgSlaPerOwnerModel> slaLogs = new ArrayList<>();
            boolean isAutoCreateCase = prepareMetaDataInProgressService.isAutoCreateCase(dto, slaLogs);
            assertFalse(isAutoCreateCase);
        }

        @Test
        void whenOwnerTeamNameIsBlank_thenFalse() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setCreateByTeamName("CreateTeam");
            dto.setOwnerTeamName(" ");
            List<StgSlaPerOwnerModel> slaLogs = new ArrayList<>();
            boolean isAutoCreateCase = prepareMetaDataInProgressService.isAutoCreateCase(dto, slaLogs);
            assertFalse(isAutoCreateCase);
        }

        @Test
        void whenOwnerTeamNameAndCreateByTeamNameAreSameAndLogHaveOtherTeam_thenFalse() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setOwnerTeamName("Team1");
            dto.setCreateByTeamName("Team1");

            StgSlaPerOwnerModel slaLog = new StgSlaPerOwnerModel();
            slaLog.setOwnerTeamC("Team2")
                    .setOwnerTeamNew("Team2");
            List<StgSlaPerOwnerModel> slaLogs = new ArrayList<>(List.of(slaLog));
            boolean isAutoCreateCase = prepareMetaDataInProgressService.isAutoCreateCase(dto, slaLogs);
            assertFalse(isAutoCreateCase);
        }
    }

    @Nested
    class shouldUseAutoCreate {
        @Test
        void isAutoCreateCaseIsTrue_thenTrue() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setOwnerTeamName("Team1");
            dto.setCreateByTeamName("Team1");
            List<StgSlaPerOwnerModel> slaLogs = new ArrayList<>();
            boolean shouldUseAutoCreate = prepareMetaDataInProgressService.shouldUseAutoCreate(dto, slaLogs);
            assertTrue(shouldUseAutoCreate);
        }

        @Test
        void isSpecialStmCodeIsTrue_thenTrue() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setServiceTypeMatrixCodeOld("18050");
            dto.setServiceTypeMatrixCode("STM00001");
            ServiceTypeMatrixModel stm = new ServiceTypeMatrixModel();
            stm.setServiceTypeMatrixCode(dto.getServiceTypeMatrixCode())
                    .setServiceTypeMatrixId(UUID.randomUUID());
            when(stmRepository.findByServiceTypeMatrixCode(anyString())).thenReturn(stm);
            boolean shouldUseAutoCreate = prepareMetaDataInProgressService.shouldUseAutoCreate(dto, new ArrayList<>());
            assertTrue(shouldUseAutoCreate);
        }

        @Test
        void isSpecialTeamNameIsTrue_thenTrue() {
            CreateCaseDTO dto = new CreateCaseDTO();
            dto.setResponsibleBuOld("TMB Contact Center-Follow up");
            boolean shouldUseAutoCreate = prepareMetaDataInProgressService.shouldUseAutoCreate(dto, new ArrayList<>());
            assertTrue(shouldUseAutoCreate);
        }
    }

    @Nested
    class getLatestStartHop {
        @Test
        void whenHaveCaseHopIsStart_thenReturnCaseHop() {
            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));

            CaseSlaHopModel secondHop = new CaseSlaHopModel();
            secondHop.setHopNumber(2)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("secondTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(null);

            CaseSlaHopModel latestStartHop = prepareMetaDataInProgressService.getLatestStartHop(List.of(firstHop, secondHop));

            assertNotNull(latestStartHop);
            assertEquals(secondHop.getHopNumber(), latestStartHop.getHopNumber());
        }
    }

    @Nested
    class getLatestEndHop {
        @Test
        void whenHaveCaseHopIsEnd_thenReturnCaseHop() {
            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));

            CaseSlaHopModel secondHop = new CaseSlaHopModel();
            secondHop.setHopNumber(2)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("secondTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-08T06:42:39Z"));

            List<CaseSlaHopModel> hopModels = new ArrayList<>(List.of(firstHop, secondHop));
            Optional<CaseSlaHopModel> latestEndHop = prepareMetaDataInProgressService.getLatestEndHop(hopModels);

            assert latestEndHop.isPresent();
            assertNotNull(latestEndHop);
            CaseSlaHopModel caseSlaHopModel = latestEndHop.get();
            assertEquals(secondHop.getHopNumber(), caseSlaHopModel.getHopNumber());
        }

        @Test
        void whenNotHaveCaseHopIsEnd_thenReturnNull() {
            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(null);

            List<CaseSlaHopModel> hopModels = new ArrayList<>(List.of(firstHop));
            Optional<CaseSlaHopModel> latestEndHop = prepareMetaDataInProgressService.getLatestEndHop(hopModels);

            assertNotNull(latestEndHop);
        }
    }

    @Nested
    class autoStartNextHopWithSTMTypeIsFix {
        @Test
        void whenStmTypeIsFixAndCaseLastActiveHopIsAlreadyEndAndNextHopNotStart_thenNextHopAutoStart() {
            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));

            CaseSlaHopModel secondHop = new CaseSlaHopModel();
            secondHop.setHopNumber(2)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("secondTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-08T06:42:39Z"));

            CaseSlaHopModel thirdHop = new CaseSlaHopModel();
            thirdHop.setHopNumber(3)
                    .setSlaTarget(0f)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("thirdTeam");

            List<CaseSlaHopModel> hopModels = new ArrayList<>(List.of(firstHop, secondHop, thirdHop));
            Optional<CaseSlaHopModel> latestEndHop = prepareMetaDataInProgressService.getLatestEndHop(hopModels);

            assertTrue(latestEndHop.isPresent());

            when(holidayRepository.findByStatusCode(0))
                    .thenReturn(Optional.of(List.of()));

            prepareMetaDataInProgressService.autoStartNextHopWithSTMTypeIsFix(hopModels, latestEndHop.get());

            assertNotNull(hopModels.getLast().getStartDatetime());
        }
    }

    @Nested
    class validationActiveHopModelIsValid {
        @Test
        void whenStmTypeIsFixAndHaveLatestStartHop_thenDoNoting() {
            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));

            prepareMetaDataInProgressService.validationActiveHopModelIsValid(List.of(firstHop), ServiceTypeMatrixTypeEnum.FIX);
            assertNotNull(firstHop.getStartDatetime());
        }

        @Test
        void whenStmTypeIsFixAndNotHaveLatestStartHopAndNotHaveLatestEndHop_thenDoNoting() {
            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(null);

            prepareMetaDataInProgressService.validationActiveHopModelIsValid(List.of(firstHop), ServiceTypeMatrixTypeEnum.FIX);

            assertNull(firstHop.getEndDatetime());
        }

        @Test
        void whenStmTypeIsFixAndNotHaveLatestStartHopAndHaveLatestEndHopAndHopSizeIsMoreLatestEndHopValueOfHopNumberPlueOne_thenDoAutoStartNextHop() {
            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));

            CaseSlaHopModel secondHop = new CaseSlaHopModel();
            secondHop.setHopNumber(2)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("secondTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-08T06:42:39Z"));

            CaseSlaHopModel thirdHop = new CaseSlaHopModel();
            thirdHop.setHopNumber(3)
                    .setSlaTarget(0f)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("thirdTeam");

            when(holidayRepository.findByStatusCode(0))
                    .thenReturn(Optional.of(List.of()));

            prepareMetaDataInProgressService.validationActiveHopModelIsValid(List.of(firstHop, secondHop, thirdHop), ServiceTypeMatrixTypeEnum.FIX);

            assertNotNull(secondHop.getStartDatetime());
        }

        // Impossible Case
        @Test
        void whenStmTypeIsDynamicAndNotHaveLatestStartHopAndHaveLatestEndHopAndHopSizeIsMoreLatestEndHopValueOfHopNumberPlueOne_thenDoNoting() {
            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));
            CaseSlaHopModel secondHop = new CaseSlaHopModel();
            secondHop.setHopNumber(2)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("secondTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-08T06:42:39Z"));

            CaseSlaHopModel thirdHop = new CaseSlaHopModel();
            thirdHop.setHopNumber(3)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("thirdTeam")
                    .setStartDatetime(null)
                    .setEndDatetime(null);

            List<CaseSlaHopModel> hopModels = new ArrayList<>(List.of(firstHop, secondHop, thirdHop));
            prepareMetaDataInProgressService.validationActiveHopModelIsValid(hopModels, ServiceTypeMatrixTypeEnum.DYNAMIC);
            assertNull(thirdHop.getStartDatetime());
        }

        @Test
        void whenStmTypeIsFixAndNotHaveLatestStartHopAndHaveLatestEndHopAndHopSizeIsLessLatestEndHopValueOfHopNumberPlueOne_thenThrowException() {
            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));

            CaseSlaHopModel secondHop = new CaseSlaHopModel();
            secondHop.setHopNumber(2)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("secondTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-08T06:42:39Z"));

            prepareMetaDataInProgressService.validationActiveHopModelIsValid(List.of(firstHop, secondHop), ServiceTypeMatrixTypeEnum.FIX);
            assertNull(secondHop.getEndDatetime());

//            ValidationException ex = assertThrows(
//                    ValidationException.class,
//                    () -> prepareMetaDataInProgressService.validationActiveHopModelIsValid(List.of(firstHop, secondHop), ServiceTypeMatrixTypeEnum.FIX)
//            );
//
//            assertEquals("Last hop of case is already end but case status is new or in_progress", ex.getMessage());
        }

        @Test
        void whenStmTypeIsDynamicAndNotHaveLatestStartHopAndHaveLatestEndHopAndHopSizeIsLessLatestEndHopValueOfHopNumberPlueOne_thenThrowException() {
            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));

            CaseSlaHopModel secondHop = new CaseSlaHopModel();
            secondHop.setHopNumber(2)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("secondTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-08T06:42:39Z"));

            prepareMetaDataInProgressService.validationActiveHopModelIsValid(List.of(firstHop, secondHop), ServiceTypeMatrixTypeEnum.DYNAMIC);
            assertNull(secondHop.getEndDatetime());

//            ValidationException ex = assertThrows(
//                    ValidationException.class,
//                    () -> prepareMetaDataInProgressService.validationActiveHopModelIsValid(List.of(firstHop, secondHop), ServiceTypeMatrixTypeEnum.DYNAMIC)
//            );
//
//            assertEquals("Service type matrix type is dynamic but case cannot next hop because no histories left", ex.getMessage());
        }
    }
    

    @Nested
    class buildServiceTypeMatrixSlaModelForDynamic {
        @Test
        void whenMatchHopEnd_thenBuildStmSlaWithSlaHopEndInfo() {
            ServiceTypeMatrixModel stm = mockSTM.mockSTMDynamicWithOutFCR();

            StgSlaPerOwnerModel slaLog = new StgSlaPerOwnerModel();
            slaLog.setOwnerTeamC("Investment Line")
                    .setOwnerTeamNew("Investment Line");

            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));

            ServiceTypeMatrixSla stmSla = prepareMetaDataInProgressService.buildServiceTypeMatrixSlaModelForDynamic(stm, slaLog, List.of(firstHop));

            ServiceTypeMatrixSla lastStmSla = stm.getServiceTypeMatrixSlas().getLast();
            assertEquals(firstHop.getHopNumber() + 1, stmSla.getHopNumber());
            assertEquals(lastStmSla.getResponsibleBuId(), stmSla.getResponsibleBuId());
            assertEquals(lastStmSla.getResponsibleBu(), stmSla.getResponsibleBu());
            assertEquals(lastStmSla.getCloseByBu(), stmSla.getCloseByBu());
            assertEquals(lastStmSla.getServiceTypeMatrixSlaId(), stmSla.getServiceTypeMatrixSlaId());
            assertEquals(lastStmSla.getSlaTarget(), stmSla.getSlaTarget());
        }

        @Test
        void whenSlaNotHopEndAndIsClosedByCreatorAndSfActivityOwnerTeamNameIsMatchWithFirstHopTeamName_thenBuildStmSlaWithSlaHopEndAndFirstHopInfo() {
            ServiceTypeMatrixModel stm = mockSTM.mockSTMClosedByCreator();

            StgSlaPerOwnerModel slaLog = new StgSlaPerOwnerModel();
            slaLog.setOwnerTeamC("firstTeam")
                    .setOwnerTeamNew("firstTeam");
            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));

            CaseSlaHopModel secondHop = new CaseSlaHopModel();
            secondHop.setHopNumber(2)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("secondTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-08T06:42:39Z"));

            ServiceTypeMatrixSla stmSla = prepareMetaDataInProgressService.buildServiceTypeMatrixSlaModelForDynamic(stm, slaLog, List.of(firstHop, secondHop));

            ServiceTypeMatrixSla lastStmSla = stm.getServiceTypeMatrixSlas().getLast();
            assertEquals(secondHop.getHopNumber() + 1, stmSla.getHopNumber());
            assertEquals(firstHop.getTeamId(), stmSla.getResponsibleBuId());
            assertEquals(firstHop.getTeamName(), stmSla.getResponsibleBu());
            assertEquals(lastStmSla.getCloseByBu(), stmSla.getCloseByBu());
            assertEquals(lastStmSla.getServiceTypeMatrixSlaId(), stmSla.getServiceTypeMatrixSlaId());
            assertEquals(lastStmSla.getSlaTarget(), stmSla.getSlaTarget());
        }

        @Test
        void whenSlaNotHopEndAndNotClosedByCreator_thenBuildStmSlaWithSlaSecondHopInfoAndTeamInfoFromPreviousHop() {
            ServiceTypeMatrixModel stm = mockSTM.mockSTMDynamicWithOutFCR();

            StgSlaPerOwnerModel slaLog = new StgSlaPerOwnerModel();
            slaLog.setOwnerTeamC("Inbound Voice 8")
                    .setOwnerTeamNew("Inbound Voice 8");

            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));
            CaseSlaHopModel secondHop = new CaseSlaHopModel();
            secondHop.setHopNumber(2)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("secondTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-08T06:42:39Z"));

            ServiceTypeMatrixSla stmSla = prepareMetaDataInProgressService.buildServiceTypeMatrixSlaModelForDynamic(stm, slaLog, List.of(firstHop, secondHop));

            assertNull(stmSla.getResponsibleBuId());
            assertNull(stmSla.getResponsibleBu());
            assertFalse(stmSla.getCloseByBu());
            assertEquals(secondHop.getHopNumber() + 1, stmSla.getHopNumber());
        }

        @Test
        void whenSlaNotHopEndAndNotClosedByCreatorAndSaleForceActivityMatchTeamNameWithSla_thenBuildStmSlaWithSlaSecondHopInfo() {
            ServiceTypeMatrixModel stm = mockSTM.mockSTMDynamicWithOutFCR();

            StgSlaPerOwnerModel slaLog = new StgSlaPerOwnerModel();
            slaLog.setOwnerTeamC(stm.getServiceTypeMatrixSlas().get(1).getResponsibleBu())
                    .setOwnerTeamNew(stm.getServiceTypeMatrixSlas().get(1).getResponsibleBu());

            CaseSlaHopModel firstHop = new CaseSlaHopModel();
            firstHop.setHopNumber(1)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("firstTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"));
            CaseSlaHopModel secondHop = new CaseSlaHopModel();
            secondHop.setHopNumber(2)
                    .setTeamId(UUID.randomUUID())
                    .setTeamName("secondTeam")
                    .setStartDatetime(ZonedDateTime.parse("2025-07-07T06:42:39Z"))
                    .setEndDatetime(ZonedDateTime.parse("2025-07-08T06:42:39Z"));

            ServiceTypeMatrixSla stmSla = prepareMetaDataInProgressService.buildServiceTypeMatrixSlaModelForDynamic(stm, slaLog, List.of(firstHop, secondHop));

            ServiceTypeMatrixSla secondStmSla = stm.getServiceTypeMatrixSlas().get(1);
            assertEquals(secondHop.getHopNumber() + 1, stmSla.getHopNumber());
            assertEquals(secondStmSla.getResponsibleBuId(), stmSla.getResponsibleBuId());
            assertEquals(secondStmSla.getResponsibleBu(), stmSla.getResponsibleBu());
            assertEquals(secondStmSla.getCloseByBu(), stmSla.getCloseByBu());
            assertEquals(secondStmSla.getServiceTypeMatrixSlaId(), stmSla.getServiceTypeMatrixSlaId());
            assertEquals(secondStmSla.getSlaTarget(), stmSla.getSlaTarget());
        }
    }
}
