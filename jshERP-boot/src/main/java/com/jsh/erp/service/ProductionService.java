package com.jsh.erp.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.ProductionBom;
import com.jsh.erp.datasource.entities.ProductionBomItem;
import com.jsh.erp.datasource.entities.ProductionOrder;
import com.jsh.erp.datasource.entities.ProductionOrderItem;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.mappers.ProductionBomMapper;
import com.jsh.erp.datasource.mappers.ProductionOrderMapper;
import com.jsh.erp.utils.PageUtils;
import com.jsh.erp.utils.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ProductionService {
    private static final String MODULE_NAME = "生产管理";
    private static final String ORDER_STATUS_DRAFT = "草稿";
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Resource
    private ProductionBomMapper productionBomMapper;
    @Resource
    private ProductionOrderMapper productionOrderMapper;
    @Resource
    private UserService userService;
    @Resource
    private LogService logService;

    public List<ProductionBom> selectBomList(String keyword) throws Exception {
        PageUtils.startPage();
        return productionBomMapper.selectBomList(StringUtil.toNull(keyword), getTenantId());
    }

    public List<ProductionBom> selectEnabledBomList() throws Exception {
        return productionBomMapper.selectEnabledBomList(getTenantId());
    }

    public JSONObject getBomDetail(Long id) {
        JSONObject detail = new JSONObject();
        ProductionBom bom = productionBomMapper.selectBomById(id);
        detail.put("info", bom);
        detail.put("items", bom == null ? new ArrayList<ProductionBomItem>() : productionBomMapper.selectBomItems(id));
        return detail;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int saveBom(JSONObject obj, HttpServletRequest request) throws Exception {
        ProductionBom bom = JSONObject.parseObject(obj.toJSONString(), ProductionBom.class);
        if (StringUtil.isEmpty(bom.getBomNo())) {
            bom.setBomNo(nextNo("BOM"));
        }
        if (bom.getOutputQuantity() == null || bom.getOutputQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            bom.setOutputQuantity(BigDecimal.ONE);
        }
        if (bom.getEnabled() == null) {
            bom.setEnabled(true);
        }
        applyAuditFields(bom);
        int result;
        if (bom.getId() == null) {
            result = productionBomMapper.insertBom(bom);
            logService.insertLog(MODULE_NAME, BusinessConstants.LOG_OPERATION_TYPE_ADD + "BOM[" + bom.getName() + "]", request);
        } else {
            result = productionBomMapper.updateBom(bom);
            productionBomMapper.deleteBomItems(bom.getId());
            productionBomMapper.purgeDeletedBomItems(bom.getId());
            logService.insertLog(MODULE_NAME, BusinessConstants.LOG_OPERATION_TYPE_EDIT + "BOM[" + bom.getName() + "]", request);
        }
        saveBomItems(bom.getId(), obj.getJSONArray("items"));
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteBom(Long id, HttpServletRequest request) throws Exception {
        ProductionBom bom = productionBomMapper.selectBomById(id);
        productionBomMapper.deleteBomItems(id);
        int result = productionBomMapper.deleteBom(id);
        logService.insertLog(MODULE_NAME, BusinessConstants.LOG_OPERATION_TYPE_DELETE + "BOM[" + (bom == null ? id : bom.getName()) + "]", request);
        return result;
    }

    public List<ProductionOrder> selectOrderList(String keyword, String status) throws Exception {
        PageUtils.startPage();
        return productionOrderMapper.selectOrderList(StringUtil.toNull(keyword), StringUtil.toNull(status), getTenantId());
    }

    public JSONObject getOrderDetail(Long id) {
        JSONObject detail = new JSONObject();
        ProductionOrder order = productionOrderMapper.selectOrderById(id);
        detail.put("info", order);
        detail.put("items", order == null ? new ArrayList<ProductionOrderItem>() : productionOrderMapper.selectOrderItems(id));
        return detail;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int saveOrder(JSONObject obj, HttpServletRequest request) throws Exception {
        ProductionOrder order = JSONObject.parseObject(obj.toJSONString(), ProductionOrder.class);
        if (StringUtil.isEmpty(order.getOrderNo())) {
            order.setOrderNo(nextNo("SC"));
        }
        if (StringUtil.isEmpty(order.getStatus())) {
            order.setStatus(ORDER_STATUS_DRAFT);
        }
        if (order.getFinishedQuantity() == null) {
            order.setFinishedQuantity(BigDecimal.ZERO);
        }
        applyAuditFields(order);
        int result;
        if (order.getId() == null) {
            result = productionOrderMapper.insertOrder(order);
            logService.insertLog(MODULE_NAME, BusinessConstants.LOG_OPERATION_TYPE_ADD + "生产单[" + order.getOrderNo() + "]", request);
        } else {
            result = productionOrderMapper.updateOrder(order);
            productionOrderMapper.deleteOrderItems(order.getId());
            logService.insertLog(MODULE_NAME, BusinessConstants.LOG_OPERATION_TYPE_EDIT + "生产单[" + order.getOrderNo() + "]", request);
        }
        saveOrderItems(order.getId(), obj.getJSONArray("items"));
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteOrder(Long id, HttpServletRequest request) throws Exception {
        ProductionOrder order = productionOrderMapper.selectOrderById(id);
        productionOrderMapper.deleteOrderItems(id);
        int result = productionOrderMapper.deleteOrder(id);
        logService.insertLog(MODULE_NAME, BusinessConstants.LOG_OPERATION_TYPE_DELETE + "生产单[" + (order == null ? id : order.getOrderNo()) + "]", request);
        return result;
    }

    public List<ProductionOrderItem> calculateMaterials(Long bomId, BigDecimal planQuantity) {
        ProductionBom bom = productionBomMapper.selectBomById(bomId);
        List<ProductionBomItem> bomItems = productionBomMapper.selectBomItems(bomId);
        List<ProductionOrderItem> items = new ArrayList<>();
        BigDecimal outputQuantity = bom == null || bom.getOutputQuantity() == null || bom.getOutputQuantity().compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ONE : bom.getOutputQuantity();
        BigDecimal quantity = planQuantity == null || planQuantity.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ONE : planQuantity;
        int sort = 0;
        for (ProductionBomItem bomItem : bomItems) {
            ProductionOrderItem item = new ProductionOrderItem();
            item.setBomItemId(bomItem.getId());
            item.setMaterialId(bomItem.getMaterialId());
            item.setMaterialExtendId(bomItem.getMaterialExtendId());
            item.setMaterialName(bomItem.getMaterialName());
            item.setMaterialUnit(bomItem.getMaterialUnit());
            BigDecimal lossRate = bomItem.getLossRate() == null ? BigDecimal.ZERO : bomItem.getLossRate();
            BigDecimal lossMultiplier = BigDecimal.ONE.add(lossRate.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP));
            BigDecimal planned = bomItem.getQuantity().multiply(quantity).divide(outputQuantity, 6, RoundingMode.HALF_UP).multiply(lossMultiplier);
            item.setPlannedQuantity(planned.setScale(6, RoundingMode.HALF_UP));
            item.setIssuedQuantity(BigDecimal.ZERO);
            item.setRemark(bomItem.getRemark());
            item.setSort(sort++);
            item.setTenantId(bomItem.getTenantId());
            item.setDeleteFlag(BusinessConstants.DELETE_FLAG_EXISTS);
            items.add(item);
        }
        return items;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateOrderStatus(Long id, String status, HttpServletRequest request) throws Exception {
        int result = productionOrderMapper.updateOrderStatus(id, status);
        logService.insertLog(MODULE_NAME, BusinessConstants.LOG_OPERATION_TYPE_EDIT + "生产单状态[" + status + "]", request);
        return result;
    }

    private void saveBomItems(Long bomId, JSONArray items) throws Exception {
        if (items == null) {
            return;
        }
        Long tenantId = getTenantId();
        for (int i = 0; i < items.size(); i++) {
            ProductionBomItem item = items.getObject(i, ProductionBomItem.class);
            item.setBomId(bomId);
            item.setSort(item.getSort() == null ? i : item.getSort());
            item.setTenantId(tenantId);
            item.setDeleteFlag(BusinessConstants.DELETE_FLAG_EXISTS);
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                item.setQuantity(BigDecimal.ONE);
            }
            if (item.getLossRate() == null) {
                item.setLossRate(BigDecimal.ZERO);
            }
            productionBomMapper.insertBomItem(item);
        }
    }

    private void saveOrderItems(Long orderId, JSONArray items) throws Exception {
        if (items == null) {
            return;
        }
        Long tenantId = getTenantId();
        for (int i = 0; i < items.size(); i++) {
            ProductionOrderItem item = items.getObject(i, ProductionOrderItem.class);
            item.setOrderId(orderId);
            item.setSort(item.getSort() == null ? i : item.getSort());
            item.setTenantId(tenantId);
            item.setDeleteFlag(BusinessConstants.DELETE_FLAG_EXISTS);
            if (item.getPlannedQuantity() == null) {
                item.setPlannedQuantity(BigDecimal.ZERO);
            }
            if (item.getIssuedQuantity() == null) {
                item.setIssuedQuantity(BigDecimal.ZERO);
            }
            productionOrderMapper.insertOrderItem(item);
        }
    }

    private void applyAuditFields(ProductionBom bom) throws Exception {
        Date now = new Date();
        User user = userService.getCurrentUser();
        bom.setUpdateTime(now);
        if (bom.getId() == null) {
            bom.setCreateTime(now);
            bom.setCreator(user == null ? null : user.getId());
            bom.setTenantId(user == null ? null : user.getTenantId());
            bom.setDeleteFlag(BusinessConstants.DELETE_FLAG_EXISTS);
        }
    }

    private void applyAuditFields(ProductionOrder order) throws Exception {
        Date now = new Date();
        User user = userService.getCurrentUser();
        order.setUpdateTime(now);
        if (order.getId() == null) {
            order.setCreateTime(now);
            order.setCreator(user == null ? null : user.getId());
            order.setTenantId(user == null ? null : user.getTenantId());
            order.setDeleteFlag(BusinessConstants.DELETE_FLAG_EXISTS);
        }
    }

    private Long getTenantId() throws Exception {
        User user = userService.getCurrentUser();
        return user == null ? null : user.getTenantId();
    }

    private String nextNo(String prefix) {
        return prefix + System.currentTimeMillis();
    }
}
