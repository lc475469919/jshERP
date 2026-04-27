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
    @TableField("image_url")
    private String imageUrl;
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
    @TableField("purchase_tax_rate")
    private BigDecimal purchaseTaxRate;
    @TableField("sale_tax_rate")
    private BigDecimal saleTaxRate;
    @TableField("min_stock")
    private BigDecimal minStock;
    @TableField("max_stock")
    private BigDecimal maxStock;
    @TableField("color_names")
    private String colorNames;
    @TableField("serial_enabled")
    private Integer serialEnabled;
    @TableField("batch_enabled")
    private Integer batchEnabled;
    @TableField("expiry_enabled")
    private Integer expiryEnabled;
    @TableField("sale_enabled")
    private Integer saleEnabled;
    @TableField("purchase_enabled")
    private Integer purchaseEnabled;
    @TableField("self_made_enabled")
    private Integer selfMadeEnabled;
    @TableField("outsourcing_enabled")
    private Integer outsourcingEnabled;
    private Integer status;
    private String remark;
    @TableField("detail_description")
    private String detailDescription;
    @TableField("production_department")
    private String productionDepartment;

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
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
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
    public BigDecimal getPurchaseTaxRate() { return purchaseTaxRate; }
    public void setPurchaseTaxRate(BigDecimal purchaseTaxRate) { this.purchaseTaxRate = purchaseTaxRate; }
    public BigDecimal getSaleTaxRate() { return saleTaxRate; }
    public void setSaleTaxRate(BigDecimal saleTaxRate) { this.saleTaxRate = saleTaxRate; }
    public BigDecimal getMinStock() { return minStock; }
    public void setMinStock(BigDecimal minStock) { this.minStock = minStock; }
    public BigDecimal getMaxStock() { return maxStock; }
    public void setMaxStock(BigDecimal maxStock) { this.maxStock = maxStock; }
    public String getColorNames() { return colorNames; }
    public void setColorNames(String colorNames) { this.colorNames = colorNames; }
    public Integer getSerialEnabled() { return serialEnabled; }
    public void setSerialEnabled(Integer serialEnabled) { this.serialEnabled = serialEnabled; }
    public Integer getBatchEnabled() { return batchEnabled; }
    public void setBatchEnabled(Integer batchEnabled) { this.batchEnabled = batchEnabled; }
    public Integer getExpiryEnabled() { return expiryEnabled; }
    public void setExpiryEnabled(Integer expiryEnabled) { this.expiryEnabled = expiryEnabled; }
    public Integer getSaleEnabled() { return saleEnabled; }
    public void setSaleEnabled(Integer saleEnabled) { this.saleEnabled = saleEnabled; }
    public Integer getPurchaseEnabled() { return purchaseEnabled; }
    public void setPurchaseEnabled(Integer purchaseEnabled) { this.purchaseEnabled = purchaseEnabled; }
    public Integer getSelfMadeEnabled() { return selfMadeEnabled; }
    public void setSelfMadeEnabled(Integer selfMadeEnabled) { this.selfMadeEnabled = selfMadeEnabled; }
    public Integer getOutsourcingEnabled() { return outsourcingEnabled; }
    public void setOutsourcingEnabled(Integer outsourcingEnabled) { this.outsourcingEnabled = outsourcingEnabled; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getDetailDescription() { return detailDescription; }
    public void setDetailDescription(String detailDescription) { this.detailDescription = detailDescription; }
    public String getProductionDepartment() { return productionDepartment; }
    public void setProductionDepartment(String productionDepartment) { this.productionDepartment = productionDepartment; }
}
