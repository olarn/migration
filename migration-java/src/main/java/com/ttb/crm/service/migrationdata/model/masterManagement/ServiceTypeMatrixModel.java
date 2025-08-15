package com.ttb.crm.service.migrationdata.model.masterManagement;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "service_type_matrix")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class ServiceTypeMatrixModel {
    @Id
    @GeneratedValue
    @Column(name = "service_type_matrix_id", nullable = false, updatable = false)
    private UUID serviceTypeMatrixId;

    @Column(name = "service_type_matrix_code", nullable = false)
    private String serviceTypeMatrixCode;

    @Column(name = "service_category_code")
    private String serviceCategoryCode;

    @Column(name = "service_category_value")
    private String serviceCategoryValue;

    @Column(name = "service_tab_code")
    private String serviceTabCode;

    @Column(name = "service_tab_value")
    private String serviceTabValue;

    @Column(name = "sub_service_tab_code")
    private String subServiceTabCode;

    @Column(name = "sub_service_tab_value")
    private String subServiceTabValue;

    @Column(name = "service_template_type_code")
    private String serviceTemplateTypeCode;

    @Column(name = "service_template_type_value")
    private String serviceTemplateTypeValue;

    @Column(name = "service_template_code")
    private String serviceTemplateCode;

    @Column(name = "service_template_value")
    private String serviceTemplateValue;

    @Column(name = "supported_channel_code")
    private String supportedChannelCode;

    @Column(name = "supported_channel_value")
    private String supportedChannelValue;

    @Column(name = "product_service_code")
    private String productServiceCode;

    @Column(name = "product_service_value_th")
    private String productServiceValueTh;

    @Column(name = "product_service_value_en")
    private String productServiceValueEn;

    @Column(name = "ntb_eligible")
    private Boolean ntbEligible;

    @Column(name = "issue_name_ttb_touch_th", length = 250)
    private String issueNameTtbTouchTh;

    @Column(name = "issue_name_ttb_touch_en", length = 250)
    private String issueNameTtbTouchEn;

    @Column(name = "issue_th", nullable = false, length = 250)
    private String issueTh;

    @Column(name = "issue_en", length = 250)
    private String issueEn;

    @Column(name = "issue_line_2_th", length = 250)
    private String issueLine2Th;

    @Column(name = "issue_line_2_en", length = 250)
    private String issueLine2En;

    @Column(name = "auto_close_case_after_resolved")
    private Boolean autoCloseCaseAfterResolved;

    @Column(name = "sla", nullable = false)
    private Float sla;

    @Column(name = "severity_code", nullable = false)
    private String severityCode;

    @Column(name = "severity_value")
    private String severityValue;

    @Column(name = "sms_code_new", length = 50)
    private String smsCodeNew;

//    @Column(name = "sms_code_in_progress", length = 50)
//    private String smsCodeInProgress;

    @Column(name = "sms_code_resolved", length = 50)
    private String smsCodeResolved;

    @Column(name = "sms_code_completed", length = 50)
    private String smsCodeCompleted;

    @Column(name = "sms_code_resolution_1", length = 50)
    private String smsCodeResolution1;

    @Column(name = "sms_code_resolution_2", length = 50)
    private String smsCodeResolution2;

    @Column(name = "resolution_1_code")
    private String resolution1Code;

    @Column(name = "resolution_1_value")
    private String resolution1Value;

    @Column(name = "resolution_2_code")
    private String resolution2Code;

    @Column(name = "resolution_2_value")
    private String resolution2Value;

    @Column(name = "message_id", length = 100)
    private String messageId;

    @Column(name = "url_link", length = 500)
    private String urlLink;

    @Column(name = "fr_required")
    private Boolean frRequired;

    @Column(name = "carousel_service_tab_code")
    private String carouselServiceTabCode;

    @Column(name = "carousel_service_tab_value")
    private String carouselServiceTabValue;

    @Column(name = "pta_segment_code")
    private String ptaSegmentCode;

    @Column(name = "pta_segment_value")
    private String ptaSegmentValue;

    @Column(name = "fcr")
    private Boolean fcr;

    @Column(name = "visible_on_touch")
    private Boolean visibleOnTouch;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type_matrix_type")
    private ServiceTypeMatrixTypeEnum serviceTypeMatrixType;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "service_type_matrix_id")
    @JsonManagedReference
    private List<ServiceTypeMatrixDocument> serviceTypeMatrixDocuments;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "service_type_matrix_id")
    @JsonManagedReference
    private List<ServiceTypeMatrixGuideline> serviceTypeMatrixGuidelines;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "service_type_matrix_id")
    @OrderBy("hopNumber ASC")
    @JsonManagedReference
    private List<ServiceTypeMatrixSla> serviceTypeMatrixSlas;

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

    public ServiceTypeMatrixModel(CreateCaseDTO request, ServiceTypeMatrixTypeEnum serviceTypeMatrixTypeEnum) {
        BeanUtils.copyProperties(request, this);
        this.serviceTypeMatrixType = serviceTypeMatrixTypeEnum;
    }

    public ServiceTypeMatrixModel(ServiceTypeMatrixModel other) {
        BeanUtils.copyProperties(other, this);
        this.serviceTypeMatrixSlas = other.serviceTypeMatrixSlas == null ? null :
                other.serviceTypeMatrixSlas.stream()
                        .map(ServiceTypeMatrixSla::new) // สร้าง object ใหม่ทั้งหมด
                        .collect(Collectors.toList());
    }

}