package com.yize.erp.masterdata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("yz_md_business_category")
public class MdBusinessCategory extends MdBaseEntity {
    @TableField("category_type")
    private String categoryType;
    @TableField("parent_id")
    private Long parentId;
    @TableField("category_code")
    private String categoryCode;
    @TableField("category_name")
    private String categoryName;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("sale_enabled")
    private Integer saleEnabled;
    @TableField("purchase_enabled")
    private Integer purchaseEnabled;

    public String getCategoryType() { return categoryType; }
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getSaleEnabled() { return saleEnabled; }
    public void setSaleEnabled(Integer saleEnabled) { this.saleEnabled = saleEnabled; }
    public Integer getPurchaseEnabled() { return purchaseEnabled; }
    public void setPurchaseEnabled(Integer purchaseEnabled) { this.purchaseEnabled = purchaseEnabled; }
}
