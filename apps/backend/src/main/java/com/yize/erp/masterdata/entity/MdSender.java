package com.yize.erp.masterdata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("yz_md_sender")
public class MdSender extends MdBaseEntity {
    @TableField("logistics_id")
    private Long logisticsId;
    @TableField("sender_name")
    private String senderName;
    private String phone;
    private String address;
    @TableField("default_flag")
    private Integer defaultFlag;

    public Long getLogisticsId() { return logisticsId; }
    public void setLogisticsId(Long logisticsId) { this.logisticsId = logisticsId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Integer getDefaultFlag() { return defaultFlag; }
    public void setDefaultFlag(Integer defaultFlag) { this.defaultFlag = defaultFlag; }
}
