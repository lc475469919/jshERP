package com.jsh.erp.datasource.mappers;

import com.jsh.erp.datasource.entities.ApprovalConfig;
import com.jsh.erp.datasource.entities.ApprovalTask;
import com.jsh.erp.datasource.entities.ApprovalTaskStep;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApprovalMapperEx {
    List<ApprovalConfig> selectConfigs(@Param("tenantId") Long tenantId);

    ApprovalConfig selectConfigByModule(@Param("tenantId") Long tenantId, @Param("moduleKey") String moduleKey);

    int insertConfig(ApprovalConfig config);

    int updateConfig(ApprovalConfig config);

    int deleteConfig(@Param("id") Long id, @Param("tenantId") Long tenantId);

    int deleteConfigSteps(@Param("moduleKey") String moduleKey, @Param("tenantId") Long tenantId);

    int insertConfigStep(ApprovalTaskStep step);

    List<ApprovalTaskStep> selectConfigSteps(@Param("moduleKey") String moduleKey, @Param("tenantId") Long tenantId);

    ApprovalTask selectTaskById(@Param("id") Long id, @Param("tenantId") Long tenantId);

    ApprovalTask selectPendingTaskByBill(@Param("billTable") String billTable,
                                         @Param("billId") Long billId,
                                         @Param("tenantId") Long tenantId);

    ApprovalTask selectLatestTaskByBill(@Param("billTable") String billTable,
                                        @Param("billId") Long billId,
                                        @Param("tenantId") Long tenantId);

    List<ApprovalTask> selectTasks(@Param("status") String status,
                                   @Param("submitterId") Long submitterId,
                                   @Param("approverRoleId") Long approverRoleId,
                                   @Param("approverRoleName") String approverRoleName,
                                   @Param("tenantId") Long tenantId);

    int countTasks(@Param("status") String status,
                   @Param("approverRoleId") Long approverRoleId,
                   @Param("approverRoleName") String approverRoleName,
                   @Param("tenantId") Long tenantId);

    int insertTask(ApprovalTask task);

    int updateTaskStatus(ApprovalTask task);

    int updateTaskCurrentStep(ApprovalTask task);

    int insertTaskStep(ApprovalTaskStep step);

    ApprovalTaskStep selectTaskStep(@Param("taskId") Long taskId,
                                    @Param("stepNo") Integer stepNo,
                                    @Param("tenantId") Long tenantId);

    List<ApprovalTaskStep> selectTaskSteps(@Param("taskId") Long taskId,
                                           @Param("tenantId") Long tenantId);

    int updateTaskStepStatus(ApprovalTaskStep step);

    int activateTaskStep(@Param("taskId") Long taskId,
                         @Param("stepNo") Integer stepNo,
                         @Param("tenantId") Long tenantId);
}
