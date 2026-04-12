package com.jsh.erp.datasource.entities;

import java.util.Date;

public class ApprovalConfig {
    private Long id;
    private String moduleKey;
    private String moduleName;
    private String billType;
    private String billSubType;
    private Long approverRoleId;
    private String approverRoleName;
    private Boolean enabled;
    private Long tenantId;
    private Date createTime;
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
}
