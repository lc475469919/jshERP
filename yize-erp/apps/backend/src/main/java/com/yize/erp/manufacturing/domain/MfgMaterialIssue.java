package com.yize.erp.manufacturing.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("yz_mfg_material_issue")
public class MfgMaterialIssue {
    @TableId
    public Long id;
    public String issueNo;
    public Long taskId;
    public Long warehouseId;
    public Long issueUserId;
    public String issueUserName;
    public LocalDateTime issueTime;
    public String status;
    public String remark;
    @TableLogic
    public Integer deleted;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
