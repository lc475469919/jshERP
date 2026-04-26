package com.yize.erp.masterdata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("yz_md_logistics_company")
public class MdLogisticsCompany extends MdBaseEntity {
    @TableField("logistics_code")
    private String logisticsCode;
    @TableField("logistics_name")
    private String logisticsName;
    @TableField("express_type")
    private String expressType;
    @TableField("express_company_name")
    private String expressCompanyName;
    private String remark;

    public String getLogisticsCode() { return logisticsCode; }
    public void setLogisticsCode(String logisticsCode) { this.logisticsCode = logisticsCode; }
    public String getLogisticsName() { return logisticsName; }
    public void setLogisticsName(String logisticsName) { this.logisticsName = logisticsName; }
    public String getExpressType() { return expressType; }
    public void setExpressType(String expressType) { this.expressType = expressType; }
    public String getExpressCompanyName() { return expressCompanyName; }
    public void setExpressCompanyName(String expressCompanyName) { this.expressCompanyName = expressCompanyName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
