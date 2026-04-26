package com.yize.erp.system.service;

import cn.dev33.satoken.stp.StpUtil;
import com.yize.erp.system.entity.SysOperationLog;
import com.yize.erp.system.entity.SysUser;
import com.yize.erp.system.mapper.SysOperationLogMapper;
import com.yize.erp.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService {
    private final SysOperationLogMapper operationLogMapper;
    private final SysUserMapper userMapper;

    public OperationLogService(SysOperationLogMapper operationLogMapper, SysUserMapper userMapper) {
        this.operationLogMapper = operationLogMapper;
        this.userMapper = userMapper;
    }

    public void record(String module, String operation, String result, String message) {
        SysOperationLog log = new SysOperationLog();
        log.setModuleName(module);
        log.setOperationType(operation);
        log.setRequestResult(result);
        log.setMessage(message);
        if (StpUtil.isLogin()) {
            long userId = StpUtil.getLoginIdAsLong();
            log.setUserId(userId);
            SysUser user = userMapper.selectById(userId);
            log.setUserName(user == null ? null : user.getName());
        }
        operationLogMapper.insert(log);
    }
}
