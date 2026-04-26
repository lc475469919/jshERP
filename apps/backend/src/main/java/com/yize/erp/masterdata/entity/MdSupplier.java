package com.yize.erp.masterdata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("yz_md_supplier")
public class MdSupplier extends MdBaseEntity {
    @TableField("supplier_code")
    private String supplierCode;
    @TableField("supplier_name")
    private String supplierName;
    @TableField("category_id")
    private Long categoryId;
    @TableField("contact_name")
    private String contactName;
    private String mobile;
    private String email;
    private String telephone;
    private String address;
    @TableField("prepaid_amount")
    private BigDecimal prepaidAmount;
    @TableField("invoice_enabled")
    private Integer invoiceEnabled;
    private Integer status;
    private String remark;

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getPrepaidAmount() { return prepaidAmount; }
    public void setPrepaidAmount(BigDecimal prepaidAmount) { this.prepaidAmount = prepaidAmount; }
    public Integer getInvoiceEnabled() { return invoiceEnabled; }
    public void setInvoiceEnabled(Integer invoiceEnabled) { this.invoiceEnabled = invoiceEnabled; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
