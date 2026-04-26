package com.yize.erp.masterdata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("yz_md_product_attr")
public class MdProductAttr extends MdBaseEntity {
    @TableField("attr_type")
    private String attrType;
    @TableField("attr_name")
    private String attrName;
    @TableField("allow_decimal")
    private Integer allowDecimal;
    @TableField("sort_order")
    private Integer sortOrder;
    private String remark;

    public String getAttrType() { return attrType; }
    public void setAttrType(String attrType) { this.attrType = attrType; }
    public String getAttrName() { return attrName; }
    public void setAttrName(String attrName) { this.attrName = attrName; }
    public Integer getAllowDecimal() { return allowDecimal; }
    public void setAllowDecimal(Integer allowDecimal) { this.allowDecimal = allowDecimal; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
