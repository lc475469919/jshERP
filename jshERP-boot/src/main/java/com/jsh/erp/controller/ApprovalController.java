package com.jsh.erp.controller;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.service.ApprovalService;
import com.jsh.erp.utils.BaseResponseInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/approval")
@Api(tags = {"审批管理"})
public class ApprovalController {
    @Resource
    private ApprovalService approvalService;

    @GetMapping(value = "/config/list")
    @ApiOperation(value = "审批配置列表")
    public BaseResponseInfo listConfigs() throws Exception {
        return approvalService.listConfigs();
    }

    @PostMapping(value = "/config/save")
    @ApiOperation(value = "保存审批配置")
    public BaseResponseInfo saveConfig(@RequestBody JSONObject jsonObject) throws Exception {
        return approvalService.saveConfig(jsonObject);
    }

    @PostMapping(value = "/config/delete")
    @ApiOperation(value = "删除审批配置")
    public BaseResponseInfo deleteConfig(@RequestBody JSONObject jsonObject) throws Exception {
        return approvalService.deleteConfig(jsonObject);
    }

    @PostMapping(value = "/task/submit")
    @ApiOperation(value = "提交审批")
    public BaseResponseInfo submit(@RequestBody JSONObject jsonObject) throws Exception {
        return approvalService.submit(jsonObject);
    }

    @GetMapping(value = "/task/my")
    @ApiOperation(value = "待我审批")
    public BaseResponseInfo myTasks(@RequestParam(value = "status", required = false) String status) throws Exception {
        return approvalService.myTasks(status);
    }

    @GetMapping(value = "/task/submitted")
    @ApiOperation(value = "我提交的审批")
    public BaseResponseInfo submittedTasks(@RequestParam(value = "status", required = false) String status) throws Exception {
        return approvalService.submittedTasks(status);
    }

    @GetMapping(value = "/task/count")
    @ApiOperation(value = "待审批数量")
    public BaseResponseInfo countPending() throws Exception {
        return approvalService.countPending();
    }

    @GetMapping(value = "/task/latest")
    @ApiOperation(value = "单据最新审批记录")
    public BaseResponseInfo latestTask(@RequestParam("billTable") String billTable,
                                       @RequestParam("billId") Long billId) throws Exception {
        return approvalService.latestTask(billTable, billId);
    }

    @PostMapping(value = "/task/approve")
    @ApiOperation(value = "审批通过")
    public BaseResponseInfo approve(@RequestBody JSONObject jsonObject) throws Exception {
        return approvalService.approve(jsonObject);
    }

    @PostMapping(value = "/task/reject")
    @ApiOperation(value = "审批驳回")
    public BaseResponseInfo reject(@RequestBody JSONObject jsonObject) throws Exception {
        return approvalService.reject(jsonObject);
    }
}
