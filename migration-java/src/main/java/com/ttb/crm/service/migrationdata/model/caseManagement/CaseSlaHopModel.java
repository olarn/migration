package com.ttb.crm.service.migrationdata.model.caseManagement;

import com.ttb.crm.service.migrationdata.helper.Utils;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "case_transaction_sla_hop")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class CaseSlaHopModel {

    @Id
    @GeneratedValue
    @Column(name = "case_sla_hop_id", nullable = false)
    private UUID caseSlaHopId;

    @Column(name = "hop_number", nullable = false)
    private Integer hopNumber;

    @Column(name = "iteration")
    private Integer iteration;

    @Column(name = "sla_target")
    private Float slaTarget;

    @Column(name = "sla_target_date")
    private ZonedDateTime slaTargetDate;

    @Column(name = "start_datetime")
    private ZonedDateTime startDatetime;

    @Column(name = "end_datetime")
    private ZonedDateTime endDatetime;

    @Column(name = "total_duration")
    private Float totalDuration;

    @Column(name = "effective_duration")
    private Float effectiveDuration;

    @Column(name = "description")
    private String description;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "service_type_matrix_sla_id")
    private UUID serviceTypeMatrixSlaId;

    @Column(name = "sms_code_in_progress")
    private String smsCodeInProgress;

    @Column(name = "close_by_bu")
    private Boolean closeByBu;

    @Column(name = "created_on")
    private ZonedDateTime createdOn;

    @Column(name = "created_by_id")
    private UUID createdById;

    @Column(name = "modified_on")
    private ZonedDateTime modifiedOn;

    @Column(name = "modified_by_id")
    private UUID modifiedById;

    @Column(name = "status_code")
    private Integer statusCode;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private CaseTransactionModel cases;

    public CaseSlaHopModel(CaseSlaHopModel caseSlaHop, CaseTransactionModel caseTransaction) {
        BeanUtils.copyProperties(caseSlaHop, this);
        this.setCases(caseTransaction);
    }

    public void cleanOwner() {
        this.setOwnerName(null);
        this.setOwnerId(null);
    }

    public void setOwnerCase(UUID userId, String ownerName) {
        this.setOwnerId(userId);
        this.setOwnerName(ownerName);
    }

    public void setTeamInfo(TeamReadonlyModel team) {
        this.setTeamId(team.getTeamId());
        this.setTeamName(team.getNameTh());
    }

    public EmployeeUserModel getOwnerUser() {
        if (this.ownerId == null) {
            return null;
        }
        EmployeeUserModel currentUser = new EmployeeUserModel();
        currentUser.setUserId(this.ownerId);
        return Utils.prepareUser(currentUser, this.ownerName);
    }

    public EmployeeUserModel getUserIfNullGetNaUser(EmployeeUserModel naUser) {
        EmployeeUserModel currentUser = new EmployeeUserModel();
        if (this.ownerId == null) {
            return naUser;
        }
        currentUser.setUserId(this.ownerId);
        return Utils.prepareUser(currentUser, this.ownerName);
    }

    public TeamReadonlyModel getCurrentTeam() {
        TeamReadonlyModel currentTeam = new TeamReadonlyModel();
        currentTeam.setTeamId(this.teamId);
        currentTeam.setNameTh(this.teamName);
        return currentTeam;
    }

}
