package com.yize.erp.manufacturing.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("yz_mfg_material_issue_item")
public class MfgMaterialIssueItem {
    @TableId
    public Long id;
    public Long issueId;
    public Long taskId;
    public Long materialId;
    public String materialName;
    public Long warehouseId;
    public String warehouseName;
    public BigDecimal planQty;
    public BigDecimal issueQty;
    public String unitName;
    public String remark;
    @TableLogic
    public Integer deleted;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
