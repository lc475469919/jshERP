package com.jsh.erp.datasource.entities;

import java.math.BigDecimal;
import java.util.Date;

public class ProductionProcessReport {
    private Long id;
    private Long orderId;
    private String orderNo;
    private String materialName;
    private Long processId;
    private String processName;
    private String workerName;
    private BigDecimal goodQuantity;
    private BigDecimal defectQuantity;
    private BigDecimal scrapQuantity;
    private Date reportTime;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private Long creator;
    private Long tenantId;
    private String deleteFlag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public BigDecimal getGoodQuantity() {
        return goodQuantity;
    }

    public void setGoodQuantity(BigDecimal goodQuantity) {
        this.goodQuantity = goodQuantity;
    }

    public BigDecimal getDefectQuantity() {
        return defectQuantity;
    }

    public void setDefectQuantity(BigDecimal defectQuantity) {
        this.defectQuantity = defectQuantity;
    }

    public BigDecimal getScrapQuantity() {
        return scrapQuantity;
    }

    public void setScrapQuantity(BigDecimal scrapQuantity) {
        this.scrapQuantity = scrapQuantity;
    }

    public Date getReportTime() {
        return reportTime;
    }

    public void setReportTime(Date reportTime) {
        this.reportTime = reportTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag;
    }
}
