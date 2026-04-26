package com.yize.erp.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yize.erp.common.ApiResponse;
import com.yize.erp.common.PageResult;
import com.yize.erp.system.dto.DictItemSaveRequest;
import com.yize.erp.system.dto.DictTypeSaveRequest;
import com.yize.erp.system.entity.SysDictItem;
import com.yize.erp.system.entity.SysDictType;
import com.yize.erp.system.mapper.SysDictItemMapper;
import com.yize.erp.system.mapper.SysDictTypeMapper;
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
@RequestMapping("/system/dicts")
public class DictController {
    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictItemMapper dictItemMapper;

    public DictController(SysDictTypeMapper dictTypeMapper, SysDictItemMapper dictItemMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictItemMapper = dictItemMapper;
    }

    @GetMapping
    public ApiResponse<PageResult<SysDictType>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword
    ) {
        LambdaQueryWrapper<SysDictType> query = new LambdaQueryWrapper<SysDictType>()
                .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                        .like(SysDictType::getDictCode, keyword)
                        .or()
                        .like(SysDictType::getDictName, keyword))
                .orderByDesc(SysDictType::getId);
        Page<SysDictType> result = dictTypeMapper.selectPage(Page.of(page, pageSize), query);
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, pageSize, result.getRecords()));
    }

    @PostMapping
    public ApiResponse<Void> createType(@RequestBody DictTypeSaveRequest request) {
        SysDictType type = new SysDictType();
        copy(type, request);
        dictTypeMapper.insert(type);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateType(@PathVariable Long id, @RequestBody DictTypeSaveRequest request) {
        SysDictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new IllegalArgumentException("字典不存在");
        }
        copy(type, request);
        dictTypeMapper.updateById(type);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteType(@PathVariable Long id) {
        Long itemCount = dictItemMapper.selectCount(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictTypeId, id));
        if (itemCount > 0) {
            throw new IllegalArgumentException("字典下存在字典项，不能删除");
        }
        dictTypeMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{dictTypeId}/items")
    public ApiResponse<List<SysDictItem>> items(@PathVariable Long dictTypeId) {
        return ApiResponse.ok(dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictTypeId, dictTypeId)
                .orderByAsc(SysDictItem::getSortOrder)
                .orderByAsc(SysDictItem::getId)));
    }

    @PostMapping("/{dictTypeId}/items")
    public ApiResponse<Void> createItem(@PathVariable Long dictTypeId, @RequestBody DictItemSaveRequest request) {
        SysDictItem item = new SysDictItem();
        copy(item, dictTypeId, request);
        dictItemMapper.insert(item);
        return ApiResponse.ok();
    }

    @PutMapping("/items/{id}")
    public ApiResponse<Void> updateItem(@PathVariable Long id, @RequestBody DictItemSaveRequest request) {
        SysDictItem item = dictItemMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("字典项不存在");
        }
        copy(item, request.dictTypeId() == null ? item.getDictTypeId() : request.dictTypeId(), request);
        dictItemMapper.updateById(item);
        return ApiResponse.ok();
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        dictItemMapper.deleteById(id);
        return ApiResponse.ok();
    }

    private void copy(SysDictType type, DictTypeSaveRequest request) {
        type.setDictCode(SystemHelper.requireText(request.dictCode(), "字典编码不能为空"));
        type.setDictName(SystemHelper.requireText(request.dictName(), "字典名称不能为空"));
        type.setStatus(SystemHelper.enabled(request.status()));
        type.setRemark(request.remark());
    }

    private void copy(SysDictItem item, Long dictTypeId, DictItemSaveRequest request) {
        item.setDictTypeId(dictTypeId);
        item.setItemLabel(SystemHelper.requireText(request.itemLabel(), "字典项名称不能为空"));
        item.setItemValue(SystemHelper.requireText(request.itemValue(), "字典项值不能为空"));
        item.setColor(request.color());
        item.setSortOrder(SystemHelper.zero(request.sortOrder()));
        item.setStatus(SystemHelper.enabled(request.status()));
        item.setRemark(request.remark());
    }
}
