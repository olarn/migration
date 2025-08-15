package com.ttb.crm.service.migrationdata.model.userManagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "employee_user")
@Getter
@Setter
@EqualsAndHashCode(of = "userId", callSuper = false)
@NoArgsConstructor
public class EmployeeUserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private UUID userId;

//    @Column(name = "first_name_en")
//    private String firstNameEn;
//
//    @Column(name = "last_name_en")
//    private String lastNameEn;

    @Column(name = "alias")
    private String alias;

    @Column(name = "department")
    private String department;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "email")
    private String email;

    @Column(name = "title")
    private String title;

    @Column(name = "username")
    private String username;

    @Column(name = "phone")
    private String phone;

    @Column(name = "mobile_phone")
    private String mobilePhone;

//    @Column(name = "manager_first_name")
//    private String managerFirstName;
//
//    @Column(name = "manager_last_name")
//    private String managerLastName;
//
    @Column(name = "manager_employee_id")
    private String managerEmployeeId;

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "first_name_th")
    private String firstNameTh;

    @Column(name = "last_name_th")
    private String lastNameTh;

    @Column(name = "region_code")
    private String regionCode;

//    @Column(name = "region_name_th")
//    private String regionNameTh;

//    @Column(name = "region_name_en")
//    private String regionNameEn;

//    @Column(name = "zone_name_th")
//    private String zoneNameTh;

//    @Column(name = "zone_name_en")
//    private String zoneNameEn;

    @Column(name = "zone_code")
    private String zoneCode;

    @Column(name = "rtl_channel")
    private String rtlChannel;

//    @Column(name = "branch_code")
//    private String branchCode;

//    @Column(name = "branch_name_th")
//    private String branchNameTh;
//
//    @Column(name = "branch_name_en")
//    private String branchNameEn;

    @Column(name = "federation_identifier")
    private String federationIdentifier;

    @Column(name = "client_id")
    private String clientId;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleReadonlyModel role;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    private ProfileModel profile;

    @ManyToMany(mappedBy = "userModels")
    private List<TeamReadonlyModel> teams;

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

    public String getFullNameTH() {
        return this.firstNameTh + " " + this.lastNameTh;
    }

}
