package com.yize.erp.masterdata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("yz_md_product")
public class MdProduct extends MdBaseEntity {
    @TableField("product_code")
    private String productCode;
    @TableField("product_name")
    private String productName;
    @TableField("mnemonic_code")
    private String mnemonicCode;
    @TableField("category_id")
    private Long categoryId;
    private String barcode;
    @TableField("brand_id")
    private Long brandId;
    private String specification;
    @TableField("supplier_id")
    private Long supplierId;
    @TableField("shelf_no")
    private String shelfNo;
    @TableField("default_unit_id")
    private Long defaultUnitId;
    @TableField("purchase_price")
    private BigDecimal purchasePrice;
    @TableField("cost_price")
    private BigDecimal costPrice;
    @TableField("wholesale_price")
    private BigDecimal wholesalePrice;
    @TableField("retail_price")
    private BigDecimal retailPrice;
    private Integer status;
    private String remark;

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getMnemonicCode() { return mnemonicCode; }
    public void setMnemonicCode(String mnemonicCode) { this.mnemonicCode = mnemonicCode; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getShelfNo() { return shelfNo; }
    public void setShelfNo(String shelfNo) { this.shelfNo = shelfNo; }
    public Long getDefaultUnitId() { return defaultUnitId; }
    public void setDefaultUnitId(Long defaultUnitId) { this.defaultUnitId = defaultUnitId; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public BigDecimal getWholesalePrice() { return wholesalePrice; }
    public void setWholesalePrice(BigDecimal wholesalePrice) { this.wholesalePrice = wholesalePrice; }
    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
