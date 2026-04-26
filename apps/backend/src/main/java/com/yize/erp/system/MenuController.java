package com.yize.erp.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yize.erp.common.ApiResponse;
import com.yize.erp.system.dto.MenuNode;
import com.yize.erp.system.dto.MenuSaveRequest;
import com.yize.erp.system.entity.SysMenu;
import com.yize.erp.system.log.LogOperation;
import com.yize.erp.system.mapper.SysMenuMapper;
import com.yize.erp.system.service.MenuTreeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/menus")
public class MenuController {
    private final SysMenuMapper menuMapper;
    private final MenuTreeService menuTreeService;

    public MenuController(SysMenuMapper menuMapper, MenuTreeService menuTreeService) {
        this.menuMapper = menuMapper;
        this.menuTreeService = menuTreeService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<MenuNode>> tree() {
        return ApiResponse.ok(menuTreeService.allTree());
    }

    @GetMapping
    public ApiResponse<List<SysMenu>> list() {
        return ApiResponse.ok(menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSortOrder)
                .orderByAsc(SysMenu::getId)));
    }

    @PostMapping
    @LogOperation(module = "菜单管理", operation = "新增菜单")
    public ApiResponse<Void> create(@RequestBody MenuSaveRequest request) {
        SysMenu menu = new SysMenu();
        copy(menu, request);
        menuMapper.insert(menu);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    @LogOperation(module = "菜单管理", operation = "编辑菜单")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody MenuSaveRequest request) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new IllegalArgumentException("菜单不存在");
        }
        copy(menu, request);
        menuMapper.updateById(menu);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    @LogOperation(module = "菜单管理", operation = "删除菜单")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new IllegalArgumentException("存在子菜单，不能删除");
        }
        menuMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void copy(SysMenu menu, MenuSaveRequest request) {
        menu.setParentId(SystemHelper.root(request.parentId()));
        menu.setMenuType(SystemHelper.requireText(request.menuType(), "菜单类型不能为空"));
        menu.setMenuName(SystemHelper.requireText(request.menuName(), "菜单名称不能为空"));
        menu.setRoutePath(request.routePath());
        menu.setComponentPath(request.componentPath());
        menu.setPermissionCode(request.permissionCode());
        menu.setIcon(request.icon());
        menu.setSortOrder(SystemHelper.zero(request.sortOrder()));
        menu.setVisible(SystemHelper.enabled(request.visible()));
        menu.setStatus(SystemHelper.enabled(request.status()));
    }
}
