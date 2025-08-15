package com.ttb.crm.service.migrationdata.model.masterManagement;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "master_group")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class MasterGroupModel {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String code;
    @Column(name = "is_prefix")
    private Boolean isPrefix;
    private String prefix;
    private String description;
    @Column(name = "running_number")
    private Integer runningNumber;
    @Column(name = "is_exclude_export")
    private Boolean isExcludeExport;

    @OneToMany(mappedBy = "masterGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MasterDataModel> masterData;

    @OneToMany(mappedBy = "masterGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MasterGroupMasterTagModel> masterGroupMasterTags;

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
