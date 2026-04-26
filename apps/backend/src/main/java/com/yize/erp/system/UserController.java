package com.yize.erp.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yize.erp.common.ApiResponse;
import com.yize.erp.common.PageResult;
import com.yize.erp.system.dto.UserSaveRequest;
import com.yize.erp.system.entity.SysUser;
import com.yize.erp.system.entity.SysUserRole;
import com.yize.erp.system.log.LogOperation;
import com.yize.erp.system.mapper.SysUserMapper;
import com.yize.erp.system.mapper.SysUserRoleMapper;
import com.yize.erp.system.util.PasswordUtil;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/users")
public class UserController {
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    public UserController(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @GetMapping
    public ApiResponse<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>()
                .eq(status != null, SysUser::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                        .like(SysUser::getAccount, keyword)
                        .or()
                        .like(SysUser::getName, keyword)
                        .or()
                        .like(SysUser::getMobile, keyword))
                .orderByDesc(SysUser::getId);
        Page<SysUser> result = userMapper.selectPage(Page.of(page, pageSize), query);
        result.getRecords().forEach(user -> {
            user.setPasswordHash(null);
            user.setPasswordSalt(null);
        });
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, pageSize, result.getRecords()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysUser> detail(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setPasswordHash(null);
        user.setPasswordSalt(null);
        return ApiResponse.ok(user);
    }

    @PostMapping
    @LogOperation(module = "用户管理", operation = "新增用户")
    public ApiResponse<Void> create(@RequestBody UserSaveRequest request) {
        SysUser user = new SysUser();
        user.setAccount(SystemHelper.requireText(request.account(), "账号不能为空"));
        user.setName(SystemHelper.requireText(request.name(), "姓名不能为空"));
        user.setMobile(request.mobile());
        String salt = PasswordUtil.newSalt();
        user.setPasswordSalt(salt);
        user.setPasswordHash(PasswordUtil.hash(salt, request.password() == null ? "123456" : request.password()));
        user.setStatus(SystemHelper.enabled(request.status()));
        user.setRemark(request.remark());
        userMapper.insert(user);
        saveRoles(user.getId(), request.roleIds());
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @LogOperation(module = "用户管理", operation = "编辑用户")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody UserSaveRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setName(SystemHelper.requireText(request.name(), "姓名不能为空"));
        user.setMobile(request.mobile());
        user.setStatus(SystemHelper.enabled(request.status()));
        user.setRemark(request.remark());
        if (request.password() != null && !request.password().isBlank()) {
            String salt = PasswordUtil.newSalt();
            user.setPasswordSalt(salt);
            user.setPasswordHash(PasswordUtil.hash(salt, request.password()));
        }
        userMapper.updateById(user);
        saveRoles(id, request.roleIds());
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @LogOperation(module = "用户管理", operation = "删除用户")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (id == 1L) {
            throw new IllegalArgumentException("初始化管理员不能删除");
        }
        userMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void saveRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds == null) {
            return;
        }
        roleIds.stream().distinct().forEach(roleId -> {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            userRoleMapper.insert(relation);
        });
    }
}
