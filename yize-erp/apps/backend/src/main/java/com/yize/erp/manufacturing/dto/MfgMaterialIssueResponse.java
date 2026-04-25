package com.yize.erp.manufacturing.dto;

import com.yize.erp.manufacturing.domain.MfgMaterialIssue;
import com.yize.erp.manufacturing.domain.MfgMaterialIssueItem;
import java.util.List;

public record MfgMaterialIssueResponse(
    MfgMaterialIssue issue,
    List<MfgMaterialIssueItem> items
) {
}
