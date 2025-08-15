package com.ttb.crm.service.migrationdata.model.caseManagement;

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
import org.springframework.beans.BeanUtils;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "case_transaction_comment")
@Getter
@Setter
@NoArgsConstructor
public class CaseCommentModel {

    @Id
    @GeneratedValue
    @Column(name = "comment_id", nullable = false)
    private UUID commentId;

    @Column(nullable = false, name = "team_id")
    private UUID teamId;

    @Column(name = "comment_desc", nullable = false)
    private String commentDesc;

    @Column(name = "team_name_th", nullable = false)
    private String teamNameTh;

    @Column(name = "team_name_en")
    private String teamNameEn;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private CaseTransactionModel cases;

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

    public CaseCommentModel(CaseCommentModel caseComment, CaseTransactionModel caseTransaction) {
        BeanUtils.copyProperties(caseComment, this);
        this.setCases(caseTransaction);
    }

}
