package com.yize.erp.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yize.erp.system.dto.MenuNode;
import com.yize.erp.system.entity.SysMenu;
import com.yize.erp.system.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MenuTreeService {
    private final SysMenuMapper menuMapper;

    public MenuTreeService(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    public List<SysMenu> listEnabledMenus() {
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSortOrder)
                .orderByAsc(SysMenu::getId));
    }

    public List<MenuNode> allTree() {
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSortOrder)
                .orderByAsc(SysMenu::getId));
        return buildTree(menus);
    }

    public List<MenuNode> buildTree(List<SysMenu> menus) {
        Map<Long, MenuNode> nodes = new LinkedHashMap<>();
        menus.stream()
                .sorted(Comparator.comparing(SysMenu::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysMenu::getId))
                .forEach(menu -> nodes.put(menu.getId(), toNode(menu)));

        List<MenuNode> roots = new ArrayList<>();
        for (MenuNode node : nodes.values()) {
            if (node.getParentId() == null || node.getParentId() == 0 || !nodes.containsKey(node.getParentId())) {
                roots.add(node);
                continue;
            }
            nodes.get(node.getParentId()).getChildren().add(node);
        }
        return roots;
    }

    public List<String> permissions(List<SysMenu> menus) {
        return menus.stream()
                .map(SysMenu::getPermissionCode)
                .filter(Objects::nonNull)
                .filter(code -> !code.isBlank())
                .distinct()
                .toList();
    }

    private MenuNode toNode(SysMenu menu) {
        MenuNode node = new MenuNode();
        node.setId(menu.getId());
        node.setParentId(menu.getParentId());
        node.setMenuType(menu.getMenuType());
        node.setMenuName(menu.getMenuName());
        node.setRoutePath(menu.getRoutePath());
        node.setComponentPath(menu.getComponentPath());
        node.setPermissionCode(menu.getPermissionCode());
        node.setIcon(menu.getIcon());
        node.setSortOrder(menu.getSortOrder());
        node.setVisible(menu.getVisible());
        node.setStatus(menu.getStatus());
        return node;
    }
}
