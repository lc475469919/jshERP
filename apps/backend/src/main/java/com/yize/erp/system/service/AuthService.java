package com.yize.erp.system.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yize.erp.system.dto.LoginRequest;
import com.yize.erp.system.dto.LoginResponse;
import com.yize.erp.system.dto.MenuNode;
import com.yize.erp.system.dto.UserProfile;
import com.yize.erp.system.entity.SysMenu;
import com.yize.erp.system.entity.SysRole;
import com.yize.erp.system.entity.SysUser;
import com.yize.erp.system.entity.SysUserRole;
import com.yize.erp.system.mapper.SysRoleMapper;
import com.yize.erp.system.mapper.SysUserMapper;
import com.yize.erp.system.mapper.SysUserRoleMapper;
import com.yize.erp.system.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final MenuTreeService menuTreeService;

    public AuthService(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysUserRoleMapper userRoleMapper,
            MenuTreeService menuTreeService
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.menuTreeService = menuTreeService;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, request.account()));
        if (user == null || !PasswordUtil.matches(user.getPasswordSalt(), request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new IllegalArgumentException("账号已停用");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        List<SysMenu> menus = menuTreeService.listEnabledMenus();
        return new LoginResponse(
                tokenInfo.getTokenName(),
                tokenInfo.getTokenValue(),
                profile(user),
                menuTreeService.buildTree(menus),
                menuTreeService.permissions(menus)
        );
    }

    public LoginResponse current() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("当前用户不存在");
        }
        List<SysMenu> menus = menuTreeService.listEnabledMenus();
        return new LoginResponse(
                StpUtil.getTokenName(),
                StpUtil.getTokenValue(),
                profile(user),
                menuTreeService.buildTree(menus),
                menuTreeService.permissions(menus)
        );
    }

    public UserProfile profile(SysUser user) {
        List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId()))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        List<String> roleNames = roleIds.isEmpty()
                ? List.of()
                : roleMapper.selectBatchIds(roleIds).stream().map(SysRole::getRoleName).toList();
        return new UserProfile(
                user.getId(),
                user.getAccount(),
                user.getName(),
                user.getMobile(),
                user.getStatus(),
                user.getLastLoginAt(),
                roleNames
        );
    }
}
