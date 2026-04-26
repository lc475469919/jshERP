package com.yize.erp.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yize.erp.common.ApiResponse;
import com.yize.erp.common.PageResult;
import com.yize.erp.system.dto.RoleSaveRequest;
import com.yize.erp.system.entity.SysRole;
import com.yize.erp.system.entity.SysRoleMenu;
import com.yize.erp.system.log.LogOperation;
import com.yize.erp.system.mapper.SysRoleMapper;
import com.yize.erp.system.mapper.SysRoleMenuMapper;
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
@RequestMapping("/system/roles")
public class RoleController {
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public RoleController(SysRoleMapper roleMapper, SysRoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    @GetMapping
    public ApiResponse<PageResult<SysRole>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<SysRole>()
                .eq(status != null, SysRole::getStatus, status)
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                        .like(SysRole::getRoleCode, keyword)
                        .or()
                        .like(SysRole::getRoleName, keyword))
                .orderByDesc(SysRole::getId);
        Page<SysRole> result = roleMapper.selectPage(Page.of(page, pageSize), query);
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, pageSize, result.getRecords()));
    }

    @GetMapping("/{id}/menu-ids")
    public ApiResponse<List<Long>> menuIds(@PathVariable Long id) {
        return ApiResponse.ok(roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, id))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList());
    }

    @PostMapping
    @LogOperation(module = "角色管理", operation = "新增角色")
    public ApiResponse<Void> create(@RequestBody RoleSaveRequest request) {
        SysRole role = new SysRole();
        role.setRoleCode(SystemHelper.requireText(request.roleCode(), "角色编码不能为空"));
        role.setRoleName(SystemHelper.requireText(request.roleName(), "角色名称不能为空"));
        role.setStatus(SystemHelper.enabled(request.status()));
        role.setRemark(request.remark());
        roleMapper.insert(role);
        saveMenus(role.getId(), request.menuIds());
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @LogOperation(module = "角色管理", operation = "编辑角色")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody RoleSaveRequest request) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        role.setRoleName(SystemHelper.requireText(request.roleName(), "角色名称不能为空"));
        role.setStatus(SystemHelper.enabled(request.status()));
        role.setRemark(request.remark());
        roleMapper.updateById(role);
        saveMenus(id, request.menuIds());
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @LogOperation(module = "角色管理", operation = "删除角色")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (id == 1L) {
            throw new IllegalArgumentException("初始化管理员角色不能删除");
        }
        roleMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void saveMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds == null) {
            return;
        }
        menuIds.stream().distinct().forEach(menuId -> {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            roleMenuMapper.insert(relation);
        });
    }
}
