package com.yize.erp.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("yz_sys_number_rule")
public class SysNumberRule extends SystemBaseEntity {
    @TableField("document_type")
    private String documentType;

    @TableField("document_name")
    private String documentName;

    private String prefix;

    @TableField("date_pattern")
    private String datePattern;

    @TableField("serial_length")
    private Integer serialLength;

    @TableField("current_serial")
    private Integer currentSerial;

    @TableField("reset_daily")
    private Integer resetDaily;

    private Integer status;
    private String remark;

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public Integer getSerialLength() {
        return serialLength;
    }

    public void setSerialLength(Integer serialLength) {
        this.serialLength = serialLength;
    }

    public Integer getCurrentSerial() {
        return currentSerial;
    }

    public void setCurrentSerial(Integer currentSerial) {
        this.currentSerial = currentSerial;
    }

    public Integer getResetDaily() {
        return resetDaily;
    }

    public void setResetDaily(Integer resetDaily) {
        this.resetDaily = resetDaily;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
