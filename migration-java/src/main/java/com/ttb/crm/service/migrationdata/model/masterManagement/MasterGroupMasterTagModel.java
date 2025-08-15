package com.ttb.crm.service.migrationdata.model.masterManagement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "master_group_master_tag")
@Getter
@Setter
@NoArgsConstructor
public class MasterGroupMasterTagModel {
    @Id
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_group_id")
    private MasterGroupModel masterGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_tag_id")
    private MasterTagModel masterTag;
}
