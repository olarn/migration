package com.ttb.crm.service.migrationdata.model.userManagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@Immutable
public class RoleReadonlyModel {

    @Id
    @Column(name = "role_id")
    private UUID roleId;
    @ManyToOne
    @JoinColumn(name = "parent_role_id")
    private RoleReadonlyModel parentRole;
    private String name;

    @OneToMany
    @JoinColumn(name = "parent_role_id")
    private List<RoleReadonlyModel> childrenRoles;

    @Column(name = "is_manager" )
    private Boolean isManager;

    @Column(name = "department_id")
    private String departmentId;

    @Column(name = "depth_level")
    private Integer depthLevel;

    @ManyToMany(mappedBy = "roleModels")
    private List<TeamReadonlyModel> teamModels;

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

}
