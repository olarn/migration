package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.bean.InProgressMigrationResult;
import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
import com.ttb.crm.service.migrationdata.helper.DateTimeUtils;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixSla;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseMigrationService {

    public InProgressMigrationResult shouldUseLegacySlaData(
            ServiceTypeMatrixModel stm,
            List<StgSlaPerOwnerModel> sfActivities
    ) {
        if (sfActivities.isEmpty()) {
            return new InProgressMigrationResult(false, List.of(), List.of());
        }

        List<ServiceTypeMatrixSla> stmSlas = stm.getServiceTypeMatrixSlas().stream()
                .sorted(Comparator.comparing(ServiceTypeMatrixSla::getHopNumber))
                .toList();

        List<String> stmHopSequence = stmSlas.stream()
                .map(ServiceTypeMatrixSla::getResponsibleBu)
                .filter(Objects::nonNull)
                .toList();

        if (stmHopSequence.size() <= 1) {
            return new InProgressMigrationResult(false, List.of(), List.of());
        }

        // ข้าม hop ตัวแรก
        List<String> hopsToCheck = stmHopSequence.subList(1, stmHopSequence.size());
        List<ServiceTypeMatrixSla> slasToCheck = stmSlas.subList(1, stmSlas.size());

        List<StgSlaPerOwnerModel> sortedSf = sfActivities.stream()
                .sorted(Comparator.comparing(a -> parseZonedDateTime(a.getStartDateTimeC())))
                .collect(Collectors.toList());

        // === DYNAMIC TYPE ===
        if (stm.getServiceTypeMatrixType() == ServiceTypeMatrixTypeEnum.DYNAMIC) {
            return typeDynamic(sortedSf, hopsToCheck, stmSlas);
        }

        return typeFixed(sortedSf, hopsToCheck, slasToCheck);
    }

    private InProgressMigrationResult typeDynamic(
            List<StgSlaPerOwnerModel> sortedSf,
            List<String> hopsToCheck,
            List<ServiceTypeMatrixSla> stmSlas
    ) {
        if (sortedSf.isEmpty()) {
            return new InProgressMigrationResult(false, List.of(), List.of());
        }

        String expectedFirstHop = hopsToCheck.getFirst();
        String sfFirstHop = sortedSf.getFirst().getOwnerTeamNew();

        if (!expectedFirstHop.equals(sfFirstHop)) {
            return new InProgressMigrationResult(false, List.of(), List.of());
        }

        // Map sfActivities to corresponding ServiceTypeMatrixSla
        List<ServiceTypeMatrixSla> matchedSlas = mapActivitiesToSlas(sortedSf, stmSlas);
        return new InProgressMigrationResult(true, sortedSf, matchedSlas);
    }

    private InProgressMigrationResult typeFixed(
            List<StgSlaPerOwnerModel> sortedSf,
            List<String> hopsToCheck,
            List<ServiceTypeMatrixSla> slasToCheck
    ) {
        // === FIX TYPE ===
        // สร้าง priority flow หลายลำดับ เช่น [BU2, BU3, BU4], [BU2, BU3], [BU2]
        List<List<String>> priorityCombos = new ArrayList<>();
        List<List<ServiceTypeMatrixSla>> slasCombos = new ArrayList<>();

        for (int i = hopsToCheck.size(); i >= 1; i--) {
            priorityCombos.add(hopsToCheck.subList(0, i));
            slasCombos.add(slasToCheck.subList(0, i));
        }

        for (int i = 0; i < priorityCombos.size(); i++) {
            List<String> combo = priorityCombos.get(i);
            List<ServiceTypeMatrixSla> slasCombo = slasCombos.get(i);

            MatchResult matchResult = findMatchedSequenceFromLatest(sortedSf, combo, slasCombo);
            if (!matchResult.activities.isEmpty()) {
                return new InProgressMigrationResult(true, matchResult.activities, matchResult.slas);
            }
        }

        return new InProgressMigrationResult(false, List.of(), List.of());
    }

    private static class MatchResult {
        List<StgSlaPerOwnerModel> activities;
        List<ServiceTypeMatrixSla> slas;

        MatchResult(List<StgSlaPerOwnerModel> activities, List<ServiceTypeMatrixSla> slas) {
            this.activities = activities;
            this.slas = slas;
        }
    }

    private MatchResult findMatchedSequenceFromLatest(
            List<StgSlaPerOwnerModel> sfList,
            List<String> combo,
            List<ServiceTypeMatrixSla> slasCombo
    ) {
        List<StgSlaPerOwnerModel> matchedActivities = new ArrayList<>();
        List<ServiceTypeMatrixSla> matchedSlas = new ArrayList<>();

        String firstHop = combo.getFirst();

        int startIdx = -1;
        for (int i = sfList.size() - 1; i >= 0; i--) {
            if (firstHop.equals(sfList.get(i).getOwnerTeamNew())) {
                startIdx = i;
                break;
            }
        }

        if (startIdx == -1) return new MatchResult(List.of(), List.of());

        matchedActivities.add(sfList.get(startIdx));
        matchedSlas.add(slasCombo.getFirst());

        int comboIdx = 1;
        for (int i = startIdx + 1; i < sfList.size() && comboIdx < combo.size(); i++) {
            if (combo.get(comboIdx).equals(sfList.get(i).getOwnerTeamNew())) {
                matchedActivities.add(sfList.get(i));
                matchedSlas.add(slasCombo.get(comboIdx));
                comboIdx++;
            }
        }

        return comboIdx == combo.size()
                ? new MatchResult(matchedActivities, matchedSlas)
                : new MatchResult(List.of(), List.of());
    }

    private List<ServiceTypeMatrixSla> mapActivitiesToSlas(
            List<StgSlaPerOwnerModel> activities,
            List<ServiceTypeMatrixSla> slas
    ) {
        List<ServiceTypeMatrixSla> matchedSlas = new ArrayList<>();

        for (StgSlaPerOwnerModel activity : activities) {
            for (ServiceTypeMatrixSla sla : slas) {
                if (sla.getResponsibleBu().equals(activity.getOwnerTeamNew())) {
                    matchedSlas.add(sla);
                    break;
                }
            }
        }

        return matchedSlas;
    }

    private ZonedDateTime parseZonedDateTime(String dateTimeStr) {
        try {
            return DateTimeUtils.parseToZoneDateTime(dateTimeStr);
        } catch (Exception e) {
            return null;
        }
    }
}