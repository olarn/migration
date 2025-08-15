package com.ttb.crm.service.migrationdata.helper;

import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixSla;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class MockSTM {

    public ServiceTypeMatrixModel mockSTMFixWithoutFCR() {
        ServiceTypeMatrixModel stmModel = new ServiceTypeMatrixModel();
        UUID stmId = UUID.randomUUID();
        stmModel.setServiceTypeMatrixCode("1");
        stmModel.setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.FIX);
        stmModel.setFcr(false);
        stmModel.setServiceTypeMatrixId(stmId);
        stmModel.setSla(5f);
        stmModel.setServiceTemplateCode("stem00002");
        stmModel.setServiceTemplateValue("Product");
        List<ServiceTypeMatrixSla> slas = new ArrayList<>();

        slas.add(mockSTMSla(1, "Create", UUID.randomUUID(), false, 1f));
        slas.add(mockSTMSla(2, "Inbound Voice Team 8", UUID.randomUUID(), false, 1f));
        slas.add(mockSTMSla(3, "Investment Line", UUID.randomUUID(), true, 1f));

        stmModel.setServiceTypeMatrixSlas(slas);

        return stmModel;
    }

    public ServiceTypeMatrixModel mockSTMDynamicWithOutFCR() {
        ServiceTypeMatrixModel stmModel = new ServiceTypeMatrixModel();
        UUID stmId = UUID.randomUUID();
        stmModel.setServiceTypeMatrixCode("2");
        stmModel.setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.DYNAMIC);
        stmModel.setFcr(false);
        stmModel.setServiceTypeMatrixId(stmId);
        stmModel.setSla(5f);
        stmModel.setServiceTemplateCode("stem00002");
        stmModel.setServiceTemplateValue("Product");

        List<ServiceTypeMatrixSla> slas = new ArrayList<>();
        slas.add(mockSTMSla(1, "Create", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(2, "Inbound Voice Team 1", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(3, "Investment Line", UUID.randomUUID(), true, null));
        stmModel.setServiceTypeMatrixSlas(slas);
        return stmModel;
    }

    public ServiceTypeMatrixModel mockSTMDynamicWithOutFCRClosedByCreator() {
        ServiceTypeMatrixModel stmModel = new ServiceTypeMatrixModel();
        UUID stmId = UUID.randomUUID();
        stmModel.setServiceTypeMatrixCode("2");
        stmModel.setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.DYNAMIC);
        stmModel.setFcr(false);
        stmModel.setServiceTypeMatrixId(stmId);
        stmModel.setSla(5f);
        stmModel.setServiceTemplateCode("stem00002");
        stmModel.setServiceTemplateValue("Product");

        List<ServiceTypeMatrixSla> slas = new ArrayList<>();
        slas.add(mockSTMSla(1, "Create", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(2, "Inbound Voice Team 1", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(3, Constant.CREATOR_TEAM, UUID.randomUUID(), true, null));
        stmModel.setServiceTypeMatrixSlas(slas);
        return stmModel;
    }

    public ServiceTypeMatrixModel mockSTMDynamicNotMatchWithoutFcr() {
        ServiceTypeMatrixModel stmModel = new ServiceTypeMatrixModel();
        UUID stmId = UUID.randomUUID();
        stmModel.setServiceTypeMatrixCode("2");
        stmModel.setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.DYNAMIC);
        stmModel.setFcr(false);
        stmModel.setServiceTypeMatrixId(stmId);
        stmModel.setSla(5f)
                .setServiceTemplateCode("stem00002")
                .setServiceTemplateValue("Product");
        List<ServiceTypeMatrixSla> slas = new ArrayList<>();
        slas.add(mockSTMSla(1, "Create", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(2, "Investment Line", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(3, "Inbound Voice Team 1", UUID.randomUUID(), true, null));
        stmModel.setServiceTypeMatrixSlas(slas);
        return stmModel;
    }

    public ServiceTypeMatrixModel mockSTMAutoCreateCase() {
        ServiceTypeMatrixModel stmModel = new ServiceTypeMatrixModel();
        UUID stmId = UUID.randomUUID();
        stmModel.setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.DYNAMIC);
        stmModel.setFcr(false);
        stmModel.setServiceTypeMatrixId(stmId);
        stmModel.setSla(5f);
        stmModel.setServiceTypeMatrixCode("27447");
        stmModel.setServiceTemplateCode("stem00002");
        stmModel.setServiceTemplateValue("Product");
        List<ServiceTypeMatrixSla> slas = new ArrayList<>();

        slas.add(mockSTMSla(1, "Create", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(2, "Inbound Voice Team 8", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(3, "Investment Line", UUID.randomUUID(), true, null));

        stmModel.setServiceTypeMatrixSlas(slas);

        return stmModel;
    }

    public ServiceTypeMatrixModel mockSTMFixFCR() {
        ServiceTypeMatrixModel stmModel = mockSTMFixWithoutFCR();
        stmModel.setFcr(true);
        stmModel.setServiceTypeMatrixSlas(List.of())
                .setServiceTemplateCode("stem00002")
                .setServiceTemplateValue("Product");
        return stmModel;
    }

    public ServiceTypeMatrixModel mockSTMOneAppMyAdvisor() {
        ServiceTypeMatrixModel serviceTypeMatrixModel = new ServiceTypeMatrixModel();
        serviceTypeMatrixModel.setServiceTypeMatrixCode("O00011")
                .setFcr(false)
                .setAutoCloseCaseAfterResolved(true)
                .setServiceTypeMatrixId(UUID.randomUUID())
                .setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.DYNAMIC)
                .setSla(2f)
                .setServiceTemplateCode("stem00002")
                .setServiceTemplateValue("Product");
        List<ServiceTypeMatrixSla> slas = new ArrayList<>();
        slas.add(mockSTMSla(1, "Create", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(2, "", UUID.randomUUID(), false, null));

        serviceTypeMatrixModel.setServiceTypeMatrixSlas(slas);
        return serviceTypeMatrixModel;

    }

    public ServiceTypeMatrixModel mockSTMFixAutoClosedAfterResolved() {
        ServiceTypeMatrixModel stmModel = new ServiceTypeMatrixModel();
        UUID stmId = UUID.randomUUID();
        stmModel.setServiceTypeMatrixCode("1");
        stmModel.setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.FIX);
        stmModel.setFcr(false);
        stmModel.setServiceTypeMatrixId(stmId);
        stmModel.setSla(5f);
        stmModel.setAutoCloseCaseAfterResolved(true);
        stmModel.setServiceTemplateCode("stem00002");
        stmModel.setServiceTemplateValue("Product");

        List<ServiceTypeMatrixSla> slas = new ArrayList<>();

        slas.add(mockSTMSla(1, "Create", UUID.randomUUID(), false, 1f));
        slas.add(mockSTMSla(2, "Inbound Voice Team 8", UUID.randomUUID(), false, 1f));
        slas.add(mockSTMSla(3, "Investment Line", UUID.randomUUID(), true, 1f));

        stmModel.setServiceTypeMatrixSlas(slas);

        return stmModel;
    }


    public ServiceTypeMatrixModel mockSTMOneAppSFEX() {
        ServiceTypeMatrixModel serviceTypeMatrixModel = new ServiceTypeMatrixModel();
        serviceTypeMatrixModel.setServiceTypeMatrixCode("O00011")
                .setFcr(false)
                .setAutoCloseCaseAfterResolved(true)
                .setServiceTypeMatrixId(UUID.randomUUID())
                .setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.DYNAMIC)
                .setSla(2f)
                .setServiceTemplateCode("stem00002")
                .setServiceTemplateValue("Product");

        List<ServiceTypeMatrixSla> slas = new ArrayList<>();
        slas.add(mockSTMSla(1, "Create", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(2, "Inbound Voice Team 8", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(3, "Investment Line", UUID.randomUUID(), true, null));

        serviceTypeMatrixModel.setServiceTypeMatrixSlas(slas);
        return serviceTypeMatrixModel;
    }

    public ServiceTypeMatrixModel mockSTMClosedByCreator() {
        ServiceTypeMatrixModel stmModel = new ServiceTypeMatrixModel();
        UUID stmId = UUID.randomUUID();
        stmModel.setServiceTypeMatrixCode("ClosedByCreator")
                .setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.FIX)
                .setFcr(false)
                .setServiceTypeMatrixId(stmId)
                .setSla(5f)
                .setServiceTemplateCode("stem00002")
                .setServiceTemplateValue("Product");

        List<ServiceTypeMatrixSla> slas = new ArrayList<>();
        slas.add(mockSTMSla(1, "Create", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(2, "Inbound Voice Team 8", UUID.randomUUID(), false, null));
        slas.add(mockSTMSla(3, "Create", UUID.randomUUID(), true, null));
        stmModel.setServiceTypeMatrixSlas(slas);
        return stmModel;
    }

    public ServiceTypeMatrixSla mockSTMSla(
            int hopNumber,
            String teamName,
            UUID teamID,
            Boolean isCloseByBu,
            Float slaTarget
    ) {
        ServiceTypeMatrixSla slaModel = new ServiceTypeMatrixSla();
        slaModel.setHopNumber(hopNumber);
        slaModel.setServiceTypeMatrixSlaId(UUID.randomUUID());
        slaModel.setSlaTarget(slaTarget);
        slaModel.setResponsibleBu(teamName);
        slaModel.setResponsibleBuId(teamID);
        slaModel.setCloseByBu(isCloseByBu);
        slaModel.setStatusCode(0);
        return slaModel;
    }
}
