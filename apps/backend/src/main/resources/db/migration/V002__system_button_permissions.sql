INSERT INTO yz_sys_menu (id, parent_id, menu_type, menu_name, route_path, component_path, permission_code, icon, sort_order, visible, status) VALUES
(101, 2, 'BUTTON', '新增用户', NULL, NULL, 'system:user:add', NULL, 1, 0, 1),
(102, 2, 'BUTTON', '编辑用户', NULL, NULL, 'system:user:edit', NULL, 2, 0, 1),
(103, 2, 'BUTTON', '删除用户', NULL, NULL, 'system:user:delete', NULL, 3, 0, 1),
(111, 3, 'BUTTON', '新增角色', NULL, NULL, 'system:role:add', NULL, 1, 0, 1),
(112, 3, 'BUTTON', '编辑角色', NULL, NULL, 'system:role:edit', NULL, 2, 0, 1),
(113, 3, 'BUTTON', '删除角色', NULL, NULL, 'system:role:delete', NULL, 3, 0, 1),
(121, 4, 'BUTTON', '新增菜单', NULL, NULL, 'system:menu:add', NULL, 1, 0, 1),
(122, 4, 'BUTTON', '编辑菜单', NULL, NULL, 'system:menu:edit', NULL, 2, 0, 1),
(123, 4, 'BUTTON', '删除菜单', NULL, NULL, 'system:menu:delete', NULL, 3, 0, 1),
(131, 5, 'BUTTON', '新增字典', NULL, NULL, 'system:dict:add', NULL, 1, 0, 1),
(132, 5, 'BUTTON', '编辑字典', NULL, NULL, 'system:dict:edit', NULL, 2, 0, 1),
(133, 5, 'BUTTON', '删除字典', NULL, NULL, 'system:dict:delete', NULL, 3, 0, 1),
(141, 6, 'BUTTON', '新增编号规则', NULL, NULL, 'system:number-rule:add', NULL, 1, 0, 1),
(142, 6, 'BUTTON', '编辑编号规则', NULL, NULL, 'system:number-rule:edit', NULL, 2, 0, 1),
(143, 6, 'BUTTON', '删除编号规则', NULL, NULL, 'system:number-rule:delete', NULL, 3, 0, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO yz_sys_role_menu (role_id, menu_id)
SELECT 1, id FROM yz_sys_menu WHERE id >= 100 AND id < 200
ON DUPLICATE KEY UPDATE deleted = 0;
