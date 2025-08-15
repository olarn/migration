package com.ttb.crm.service.migrationdata.model.userManagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "profileId", callSuper = false)
public class ProfileModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "profile_id")
    private UUID profileId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

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

//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(name = "profile_read_permission", joinColumns = @JoinColumn(name = "profile_id"), inverseJoinColumns = @JoinColumn(name = "read_permission_id"))
//    private List<ReadPermissionModel> readPermissions;
//
//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(name = "profile_action_permission", joinColumns = @JoinColumn(name = "profile_id"), inverseJoinColumns = @JoinColumn(name = "action_permission_id"))
//    private List<ActionPermissionModel> actionPermissions;

//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "profile_permission_endpoint",
//            joinColumns = @JoinColumn(name = "profile_id"),
//            inverseJoinColumns = @JoinColumn(name = "permission_endpoint_id"))
//    private List<PermissionEndpointModel> permissionEndpoints;
}

