package com.yize.erp.masterdata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("yz_md_customer")
public class MdCustomer extends MdBaseEntity {
    @TableField("customer_code")
    private String customerCode;
    @TableField("customer_name")
    private String customerName;
    @TableField("category_id")
    private Long categoryId;
    @TableField("contact_name")
    private String contactName;
    private String mobile;
    private String telephone;
    private String email;
    private String country;
    private String address;
    @TableField("salesperson_name")
    private String salespersonName;
    @TableField("logistics_company")
    private String logisticsCompany;
    @TableField("linked_supplier")
    private String linkedSupplier;
    @TableField("prepaid_amount")
    private BigDecimal prepaidAmount;
    @TableField("member_level")
    private String memberLevel;
    @TableField("self_order_enabled")
    private Integer selfOrderEnabled;
    @TableField("login_account")
    private String loginAccount;
    @TableField("credit_limit")
    private BigDecimal creditLimit;
    @TableField("invoice_enabled")
    private Integer invoiceEnabled;
    private Integer status;
    private String tags;
    @TableField("follow_count")
    private Integer followCount;
    @TableField("total_deal_amount")
    private BigDecimal totalDealAmount;
    @TableField("last_deal_date")
    private String lastDealDate;
    private String remark;

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getSalespersonName() { return salespersonName; }
    public void setSalespersonName(String salespersonName) { this.salespersonName = salespersonName; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }
    public String getLinkedSupplier() { return linkedSupplier; }
    public void setLinkedSupplier(String linkedSupplier) { this.linkedSupplier = linkedSupplier; }
    public BigDecimal getPrepaidAmount() { return prepaidAmount; }
    public void setPrepaidAmount(BigDecimal prepaidAmount) { this.prepaidAmount = prepaidAmount; }
    public String getMemberLevel() { return memberLevel; }
    public void setMemberLevel(String memberLevel) { this.memberLevel = memberLevel; }
    public Integer getSelfOrderEnabled() { return selfOrderEnabled; }
    public void setSelfOrderEnabled(Integer selfOrderEnabled) { this.selfOrderEnabled = selfOrderEnabled; }
    public String getLoginAccount() { return loginAccount; }
    public void setLoginAccount(String loginAccount) { this.loginAccount = loginAccount; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public Integer getInvoiceEnabled() { return invoiceEnabled; }
    public void setInvoiceEnabled(Integer invoiceEnabled) { this.invoiceEnabled = invoiceEnabled; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Integer getFollowCount() { return followCount; }
    public void setFollowCount(Integer followCount) { this.followCount = followCount; }
    public BigDecimal getTotalDealAmount() { return totalDealAmount; }
    public void setTotalDealAmount(BigDecimal totalDealAmount) { this.totalDealAmount = totalDealAmount; }
    public String getLastDealDate() { return lastDealDate; }
    public void setLastDealDate(String lastDealDate) { this.lastDealDate = lastDealDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
