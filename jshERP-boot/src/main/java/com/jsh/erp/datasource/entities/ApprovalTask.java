package com.jsh.erp.datasource.entities;

import java.math.BigDecimal;
import java.util.Date;

public class ApprovalTask {
    private Long id;
    private String billTable;
    private Long billId;
    private String billNo;
    private String billType;
    private String billSubType;
    private String moduleKey;
    private String moduleName;
    private BigDecimal billAmount;
    private Long submitterId;
    private String submitterName;
    private Long approverRoleId;
    private String approverRoleName;
    private Long approverId;
    private String approverName;
    private String status;
    private String remark;
    private String approveRemark;
    private Integer currentStepNo;
    private Integer totalStep;
    private Long tenantId;
    private Date createTime;
    private Date updateTime;
    private Date approveTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillTable() {
        return billTable;
    }

    public void setBillTable(String billTable) {
        this.billTable = billTable;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public String getBillSubType() {
        return billSubType;
    }

    public void setBillSubType(String billSubType) {
        this.billSubType = billSubType;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public void setModuleKey(String moduleKey) {
        this.moduleKey = moduleKey;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public BigDecimal getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(BigDecimal billAmount) {
        this.billAmount = billAmount;
    }

    public Long getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(Long submitterId) {
        this.submitterId = submitterId;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public Long getApproverRoleId() {
        return approverRoleId;
    }

    public void setApproverRoleId(Long approverRoleId) {
        this.approverRoleId = approverRoleId;
    }

    public String getApproverRoleName() {
        return approverRoleName;
    }

    public void setApproverRoleName(String approverRoleName) {
        this.approverRoleName = approverRoleName;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getApproveRemark() {
        return approveRemark;
    }

    public void setApproveRemark(String approveRemark) {
        this.approveRemark = approveRemark;
    }

    public Integer getCurrentStepNo() {
        return currentStepNo;
    }

    public void setCurrentStepNo(Integer currentStepNo) {
        this.currentStepNo = currentStepNo;
    }

    public Integer getTotalStep() {
        return totalStep;
    }

    public void setTotalStep(Integer totalStep) {
        this.totalStep = totalStep;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(Date approveTime) {
        this.approveTime = approveTime;
    }
}
