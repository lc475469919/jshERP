package com.jsh.erp.controller;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.ProductionBom;
import com.jsh.erp.datasource.entities.ProductionMaterialRecord;
import com.jsh.erp.datasource.entities.ProductionOrder;
import com.jsh.erp.datasource.entities.ProductionProcess;
import com.jsh.erp.service.ProductionService;
import com.jsh.erp.utils.BaseResponseInfo;
import com.jsh.erp.utils.Constants;
import com.jsh.erp.utils.ErpInfo;
import com.jsh.erp.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

@RestController
@RequestMapping(value = "/production")
@Api(tags = {"生产管理"})
public class ProductionController extends BaseController {
    @Resource
    private ProductionService productionService;

    @GetMapping(value = "/bom/list")
    @ApiOperation(value = "BOM列表")
    public TableDataInfo bomList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                 HttpServletRequest request) throws Exception {
        String keyword = StringUtil.getInfo(search, "keyword");
        List<ProductionBom> list = productionService.selectBomList(keyword);
        return getDataTable(list);
    }

    @GetMapping(value = "/bom/enabledList")
    @ApiOperation(value = "启用BOM列表")
    public BaseResponseInfo enabledBomList(HttpServletRequest request) throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = productionService.selectEnabledBomList();
        return res;
    }

    @GetMapping(value = "/bom/info")
    @ApiOperation(value = "BOM详情")
    public String bomInfo(@RequestParam("id") Long id, HttpServletRequest request) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("info", productionService.getBomDetail(id));
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    @PostMapping(value = "/bom/save")
    @ApiOperation(value = "保存BOM")
    public String saveBom(@RequestBody JSONObject obj, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int result = productionService.saveBom(obj, request);
        return returnStr(objectMap, result);
    }

    @DeleteMapping(value = "/bom/delete")
    @ApiOperation(value = "删除BOM")
    public String deleteBom(@RequestParam("id") Long id, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int result = productionService.deleteBom(id, request);
        return returnStr(objectMap, result);
    }

    @GetMapping(value = "/order/list")
    @ApiOperation(value = "生产任务列表")
    public TableDataInfo orderList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                   HttpServletRequest request) throws Exception {
        String keyword = StringUtil.getInfo(search, "keyword");
        String status = StringUtil.getInfo(search, "status");
        List<ProductionOrder> list = productionService.selectOrderList(keyword, status);
        return getDataTable(list);
    }

    @GetMapping(value = "/order/info")
    @ApiOperation(value = "生产任务详情")
    public String orderInfo(@RequestParam("id") Long id, HttpServletRequest request) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("info", productionService.getOrderDetail(id));
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    @GetMapping(value = "/materialRecord/list")
    @ApiOperation(value = "用料登记列表")
    public TableDataInfo materialRecordList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                            HttpServletRequest request) throws Exception {
        String keyword = StringUtil.getInfo(search, "keyword");
        Long orderId = StringUtil.parseStrLong(StringUtil.getInfo(search, "orderId"));
        List<ProductionMaterialRecord> list = productionService.selectMaterialRecordList(keyword, orderId);
        return getDataTable(list);
    }

    @GetMapping(value = "/process/list")
    @ApiOperation(value = "工序列表")
    public TableDataInfo processList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                     HttpServletRequest request) throws Exception {
        String keyword = StringUtil.getInfo(search, "keyword");
        List<ProductionProcess> list = productionService.selectProcessList(keyword);
        return getDataTable(list);
    }

    @GetMapping(value = "/process/enabledList")
    @ApiOperation(value = "启用工序列表")
    public BaseResponseInfo enabledProcessList(HttpServletRequest request) throws Exception {
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = productionService.selectEnabledProcessList();
        return res;
    }

    @PostMapping(value = "/order/save")
    @ApiOperation(value = "保存生产任务")
    public String saveOrder(@RequestBody JSONObject obj, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int result = productionService.saveOrder(obj, request);
        return returnStr(objectMap, result);
    }

    @DeleteMapping(value = "/order/delete")
    @ApiOperation(value = "删除生产任务")
    public String deleteOrder(@RequestParam("id") Long id, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int result = productionService.deleteOrder(id, request);
        return returnStr(objectMap, result);
    }

    @GetMapping(value = "/order/calculateMaterials")
    @ApiOperation(value = "按BOM计算生产用料")
    public BaseResponseInfo calculateMaterials(@RequestParam("bomId") Long bomId,
                                               @RequestParam(value = "planQuantity", required = false) BigDecimal planQuantity,
                                               HttpServletRequest request) {
        BaseResponseInfo res = new BaseResponseInfo();
        res.code = 200;
        res.data = productionService.calculateMaterials(bomId, planQuantity);
        return res;
    }

    @PostMapping(value = "/order/status")
    @ApiOperation(value = "更新生产任务状态")
    public String updateOrderStatus(@RequestBody JSONObject obj, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int result = productionService.updateOrderStatus(obj.getLong("id"), obj.getString("status"), request);
        return returnStr(objectMap, result);
    }

    @PostMapping(value = "/materialRecord/save")
    @ApiOperation(value = "保存用料登记")
    public String saveMaterialRecord(@RequestBody JSONObject obj, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int result = productionService.saveMaterialRecord(obj, request);
        return returnStr(objectMap, result);
    }

    @DeleteMapping(value = "/materialRecord/delete")
    @ApiOperation(value = "删除用料登记")
    public String deleteMaterialRecord(@RequestParam("id") Long id, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int result = productionService.deleteMaterialRecord(id, request);
        return returnStr(objectMap, result);
    }

    @PostMapping(value = "/process/save")
    @ApiOperation(value = "保存工序")
    public String saveProcess(@RequestBody JSONObject obj, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int result = productionService.saveProcess(obj, request);
        return returnStr(objectMap, result);
    }

    @DeleteMapping(value = "/process/delete")
    @ApiOperation(value = "删除工序")
    public String deleteProcess(@RequestParam("id") Long id, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int result = productionService.deleteProcess(id, request);
        return returnStr(objectMap, result);
    }
}
