package com.jsh.erp.datasource.mappers;

import com.jsh.erp.datasource.entities.ProductionOrder;
import com.jsh.erp.datasource.entities.ProductionOrderItem;
import com.jsh.erp.datasource.entities.ProductionMaterialRecord;
import com.jsh.erp.datasource.entities.ProductionProcess;
import com.jsh.erp.datasource.entities.ProductionProcessReport;
import com.jsh.erp.datasource.entities.ProductionQualityInspection;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

public interface ProductionOrderMapper {
    @Select("select id, order_no orderNo, bom_id bomId, material_id materialId, material_extend_id materialExtendId, " +
            "material_name materialName, material_unit materialUnit, plan_quantity planQuantity, finished_quantity finishedQuantity, " +
            "plan_start_date planStartDate, plan_finish_date planFinishDate, status, remark, create_time createTime, update_time updateTime, " +
            "creator, tenant_id tenantId, delete_flag deleteFlag from jsh_production_order where ifnull(delete_flag,'0') != '1' " +
            "and (#{tenantId} is null or tenant_id = #{tenantId}) " +
            "and (#{status} is null or #{status} = '' or status = #{status}) " +
            "and (#{keyword} is null or #{keyword} = '' or order_no like concat('%', #{keyword}, '%') or material_name like concat('%', #{keyword}, '%')) " +
            "order by id desc")
    List<ProductionOrder> selectOrderList(@Param("keyword") String keyword, @Param("status") String status, @Param("tenantId") Long tenantId);

    @Select("select id, order_no orderNo, bom_id bomId, material_id materialId, material_extend_id materialExtendId, " +
            "material_name materialName, material_unit materialUnit, plan_quantity planQuantity, finished_quantity finishedQuantity, " +
            "plan_start_date planStartDate, plan_finish_date planFinishDate, status, remark, create_time createTime, update_time updateTime, " +
            "creator, tenant_id tenantId, delete_flag deleteFlag from jsh_production_order where ifnull(delete_flag,'0') != '1' and id=#{id}")
    ProductionOrder selectOrderById(@Param("id") Long id);

    @Select("select id, order_no orderNo, bom_id bomId, material_id materialId, material_extend_id materialExtendId, " +
            "material_name materialName, material_unit materialUnit, plan_quantity planQuantity, finished_quantity finishedQuantity, " +
            "plan_start_date planStartDate, plan_finish_date planFinishDate, status, remark, create_time createTime, update_time updateTime, " +
            "creator, tenant_id tenantId, delete_flag deleteFlag from jsh_production_order where ifnull(delete_flag,'0') != '1' " +
            "and order_no=#{orderNo} and (#{tenantId} is null or tenant_id=#{tenantId}) limit 1")
    ProductionOrder selectOrderByNo(@Param("orderNo") String orderNo, @Param("tenantId") Long tenantId);

    @Insert("insert into jsh_production_order (order_no, bom_id, material_id, material_extend_id, material_name, material_unit, " +
            "plan_quantity, finished_quantity, plan_start_date, plan_finish_date, status, remark, create_time, update_time, creator, tenant_id, delete_flag) values " +
            "(#{order.orderNo}, #{order.bomId}, #{order.materialId}, #{order.materialExtendId}, #{order.materialName}, #{order.materialUnit}, " +
            "#{order.planQuantity}, #{order.finishedQuantity}, #{order.planStartDate}, #{order.planFinishDate}, #{order.status}, #{order.remark}, " +
            "#{order.createTime}, #{order.updateTime}, #{order.creator}, #{order.tenantId}, #{order.deleteFlag})")
    @Options(useGeneratedKeys = true, keyProperty = "order.id")
    int insertOrder(@Param("order") ProductionOrder order);

    @Update("update jsh_production_order set order_no=#{order.orderNo}, bom_id=#{order.bomId}, material_id=#{order.materialId}, " +
            "material_extend_id=#{order.materialExtendId}, material_name=#{order.materialName}, material_unit=#{order.materialUnit}, " +
            "plan_quantity=#{order.planQuantity}, finished_quantity=#{order.finishedQuantity}, plan_start_date=#{order.planStartDate}, " +
            "plan_finish_date=#{order.planFinishDate}, status=#{order.status}, remark=#{order.remark}, update_time=#{order.updateTime} where id=#{order.id}")
    int updateOrder(@Param("order") ProductionOrder order);

    @Update("update jsh_production_order set delete_flag='1', update_time=now() where id=#{id}")
    int deleteOrder(@Param("id") Long id);

    @Update("update jsh_production_order set status=#{status}, update_time=now() where id=#{id}")
    int updateOrderStatus(@Param("id") Long id, @Param("status") String status);

    @Update("update jsh_production_order set finished_quantity=#{finishedQuantity}, status=#{status}, update_time=now() where id=#{id}")
    int updateOrderFinishedAndStatus(@Param("id") Long id, @Param("finishedQuantity") BigDecimal finishedQuantity,
                                     @Param("status") String status);

    @Select("select ifnull(sum(i.oper_number), 0) from jsh_depot_head h " +
            "left join jsh_depot_item i on h.id = i.header_id and ifnull(i.delete_flag,'0') != '1' " +
            "where ifnull(h.delete_flag,'0') != '1' and h.type='入库' and h.sub_type='成品入库' " +
            "and h.link_number=#{orderNo} and (#{tenantId} is null or h.tenant_id=#{tenantId} or h.tenant_id is null) " +
            "and (#{materialExtendId} is null or i.material_extend_id=#{materialExtendId})")
    BigDecimal sumFinishedInQuantity(@Param("orderNo") String orderNo, @Param("materialExtendId") Long materialExtendId,
                                     @Param("tenantId") Long tenantId);

    @Select("select id, order_id orderId, bom_item_id bomItemId, material_id materialId, material_extend_id materialExtendId, " +
            "material_name materialName, material_unit materialUnit, planned_quantity plannedQuantity, issued_quantity issuedQuantity, " +
            "remark, sort, tenant_id tenantId, delete_flag deleteFlag from jsh_production_order_item " +
            "where ifnull(delete_flag,'0') != '1' and order_id=#{orderId} order by sort asc, id asc")
    List<ProductionOrderItem> selectOrderItems(@Param("orderId") Long orderId);

    @Select("select id, order_id orderId, bom_item_id bomItemId, material_id materialId, material_extend_id materialExtendId, " +
            "material_name materialName, material_unit materialUnit, planned_quantity plannedQuantity, issued_quantity issuedQuantity, " +
            "remark, sort, tenant_id tenantId, delete_flag deleteFlag from jsh_production_order_item " +
            "where ifnull(delete_flag,'0') != '1' and id=#{id}")
    ProductionOrderItem selectOrderItemById(@Param("id") Long id);

    @Insert("insert into jsh_production_order_item (order_id, bom_item_id, material_id, material_extend_id, material_name, material_unit, " +
            "planned_quantity, issued_quantity, remark, sort, tenant_id, delete_flag) values (#{item.orderId}, #{item.bomItemId}, " +
            "#{item.materialId}, #{item.materialExtendId}, #{item.materialName}, #{item.materialUnit}, #{item.plannedQuantity}, " +
            "#{item.issuedQuantity}, #{item.remark}, #{item.sort}, #{item.tenantId}, #{item.deleteFlag})")
    int insertOrderItem(@Param("item") ProductionOrderItem item);

    @Update("update jsh_production_order_item set delete_flag='1' where order_id=#{orderId}")
    int deleteOrderItems(@Param("orderId") Long orderId);

    @Update("update jsh_production_order_item set issued_quantity = ifnull(issued_quantity, 0) + #{quantity} where id=#{id}")
    int addOrderItemIssuedQuantity(@Param("id") Long id, @Param("quantity") java.math.BigDecimal quantity);

    @Select("select r.id, r.order_id orderId, r.order_item_id orderItemId, o.order_no orderNo, " +
            "r.material_id materialId, r.material_extend_id materialExtendId, r.material_name materialName, r.material_unit materialUnit, " +
            "r.quantity, r.record_time recordTime, r.remark, r.create_time createTime, r.update_time updateTime, " +
            "r.creator, r.tenant_id tenantId, r.delete_flag deleteFlag from jsh_production_material_record r " +
            "left join jsh_production_order o on r.order_id = o.id " +
            "where ifnull(r.delete_flag,'0') != '1' and (#{tenantId} is null or r.tenant_id = #{tenantId}) " +
            "and (#{orderId} is null or r.order_id = #{orderId}) " +
            "and (#{keyword} is null or #{keyword} = '' or o.order_no like concat('%', #{keyword}, '%') or r.material_name like concat('%', #{keyword}, '%')) " +
            "order by r.id desc")
    List<ProductionMaterialRecord> selectMaterialRecordList(@Param("keyword") String keyword, @Param("orderId") Long orderId, @Param("tenantId") Long tenantId);

    @Select("select id, order_id orderId, order_item_id orderItemId, material_id materialId, material_extend_id materialExtendId, " +
            "material_name materialName, material_unit materialUnit, quantity, record_time recordTime, remark, create_time createTime, " +
            "update_time updateTime, creator, tenant_id tenantId, delete_flag deleteFlag from jsh_production_material_record " +
            "where ifnull(delete_flag,'0') != '1' and id=#{id}")
    ProductionMaterialRecord selectMaterialRecordById(@Param("id") Long id);

    @Insert("insert into jsh_production_material_record (order_id, order_item_id, material_id, material_extend_id, material_name, material_unit, " +
            "quantity, record_time, remark, create_time, update_time, creator, tenant_id, delete_flag) values " +
            "(#{record.orderId}, #{record.orderItemId}, #{record.materialId}, #{record.materialExtendId}, #{record.materialName}, " +
            "#{record.materialUnit}, #{record.quantity}, #{record.recordTime}, #{record.remark}, #{record.createTime}, #{record.updateTime}, " +
            "#{record.creator}, #{record.tenantId}, #{record.deleteFlag})")
    @Options(useGeneratedKeys = true, keyProperty = "record.id")
    int insertMaterialRecord(@Param("record") ProductionMaterialRecord record);

    @Update("update jsh_production_material_record set order_id=#{record.orderId}, order_item_id=#{record.orderItemId}, " +
            "material_id=#{record.materialId}, material_extend_id=#{record.materialExtendId}, material_name=#{record.materialName}, " +
            "material_unit=#{record.materialUnit}, quantity=#{record.quantity}, record_time=#{record.recordTime}, " +
            "remark=#{record.remark}, update_time=#{record.updateTime} where id=#{record.id}")
    int updateMaterialRecord(@Param("record") ProductionMaterialRecord record);

    @Update("update jsh_production_material_record set delete_flag='1', update_time=now() where id=#{id}")
    int deleteMaterialRecord(@Param("id") Long id);

    @Select("select id, process_no processNo, name, wage_type wageType, unit_price unitPrice, enabled, sort, remark, " +
            "create_time createTime, update_time updateTime, creator, tenant_id tenantId, delete_flag deleteFlag " +
            "from jsh_production_process where ifnull(delete_flag,'0') != '1' " +
            "and (#{tenantId} is null or tenant_id = #{tenantId}) " +
            "and (#{keyword} is null or #{keyword} = '' or process_no like concat('%', #{keyword}, '%') or name like concat('%', #{keyword}, '%')) " +
            "order by sort asc, id desc")
    List<ProductionProcess> selectProcessList(@Param("keyword") String keyword, @Param("tenantId") Long tenantId);

    @Select("select id, process_no processNo, name, wage_type wageType, unit_price unitPrice, enabled, sort, remark, " +
            "create_time createTime, update_time updateTime, creator, tenant_id tenantId, delete_flag deleteFlag " +
            "from jsh_production_process where ifnull(delete_flag,'0') != '1' and enabled = 1 " +
            "and (#{tenantId} is null or tenant_id = #{tenantId}) order by sort asc, id desc")
    List<ProductionProcess> selectEnabledProcessList(@Param("tenantId") Long tenantId);

    @Select("select id, process_no processNo, name, wage_type wageType, unit_price unitPrice, enabled, sort, remark, " +
            "create_time createTime, update_time updateTime, creator, tenant_id tenantId, delete_flag deleteFlag " +
            "from jsh_production_process where ifnull(delete_flag,'0') != '1' and id=#{id}")
    ProductionProcess selectProcessById(@Param("id") Long id);

    @Insert("insert into jsh_production_process (process_no, name, wage_type, unit_price, enabled, sort, remark, " +
            "create_time, update_time, creator, tenant_id, delete_flag) values (#{process.processNo}, #{process.name}, #{process.wageType}, " +
            "#{process.unitPrice}, #{process.enabled}, #{process.sort}, #{process.remark}, #{process.createTime}, " +
            "#{process.updateTime}, #{process.creator}, #{process.tenantId}, #{process.deleteFlag})")
    @Options(useGeneratedKeys = true, keyProperty = "process.id")
    int insertProcess(@Param("process") ProductionProcess process);

    @Update("update jsh_production_process set process_no=#{process.processNo}, name=#{process.name}, wage_type=#{process.wageType}, " +
            "unit_price=#{process.unitPrice}, enabled=#{process.enabled}, sort=#{process.sort}, remark=#{process.remark}, " +
            "update_time=#{process.updateTime} where id=#{process.id}")
    int updateProcess(@Param("process") ProductionProcess process);

    @Update("update jsh_production_process set delete_flag='1', update_time=now() where id=#{id}")
    int deleteProcess(@Param("id") Long id);

    @Select("select r.id, r.order_id orderId, o.order_no orderNo, o.material_name materialName, " +
            "r.process_id processId, r.process_name processName, r.worker_name workerName, r.good_quantity goodQuantity, " +
            "r.defect_quantity defectQuantity, r.scrap_quantity scrapQuantity, r.report_time reportTime, r.remark, " +
            "r.create_time createTime, r.update_time updateTime, r.creator, r.tenant_id tenantId, r.delete_flag deleteFlag " +
            "from jsh_production_process_report r left join jsh_production_order o on r.order_id = o.id " +
            "where ifnull(r.delete_flag,'0') != '1' and (#{tenantId} is null or r.tenant_id = #{tenantId}) " +
            "and (#{orderId} is null or r.order_id = #{orderId}) " +
            "and (#{processId} is null or r.process_id = #{processId}) " +
            "and (#{keyword} is null or #{keyword} = '' or o.order_no like concat('%', #{keyword}, '%') " +
            "or o.material_name like concat('%', #{keyword}, '%') or r.process_name like concat('%', #{keyword}, '%') " +
            "or r.worker_name like concat('%', #{keyword}, '%')) order by r.id desc")
    List<ProductionProcessReport> selectProcessReportList(@Param("keyword") String keyword, @Param("orderId") Long orderId,
                                                          @Param("processId") Long processId, @Param("tenantId") Long tenantId);

    @Select("select id, order_id orderId, process_id processId, process_name processName, worker_name workerName, " +
            "good_quantity goodQuantity, defect_quantity defectQuantity, scrap_quantity scrapQuantity, report_time reportTime, " +
            "remark, create_time createTime, update_time updateTime, creator, tenant_id tenantId, delete_flag deleteFlag " +
            "from jsh_production_process_report where ifnull(delete_flag,'0') != '1' and id=#{id}")
    ProductionProcessReport selectProcessReportById(@Param("id") Long id);

    @Insert("insert into jsh_production_process_report (order_id, process_id, process_name, worker_name, good_quantity, " +
            "defect_quantity, scrap_quantity, report_time, remark, create_time, update_time, creator, tenant_id, delete_flag) values " +
            "(#{report.orderId}, #{report.processId}, #{report.processName}, #{report.workerName}, #{report.goodQuantity}, " +
            "#{report.defectQuantity}, #{report.scrapQuantity}, #{report.reportTime}, #{report.remark}, #{report.createTime}, " +
            "#{report.updateTime}, #{report.creator}, #{report.tenantId}, #{report.deleteFlag})")
    @Options(useGeneratedKeys = true, keyProperty = "report.id")
    int insertProcessReport(@Param("report") ProductionProcessReport report);

    @Update("update jsh_production_process_report set order_id=#{report.orderId}, process_id=#{report.processId}, " +
            "process_name=#{report.processName}, worker_name=#{report.workerName}, good_quantity=#{report.goodQuantity}, " +
            "defect_quantity=#{report.defectQuantity}, scrap_quantity=#{report.scrapQuantity}, report_time=#{report.reportTime}, " +
            "remark=#{report.remark}, update_time=#{report.updateTime} where id=#{report.id}")
    int updateProcessReport(@Param("report") ProductionProcessReport report);

    @Update("update jsh_production_process_report set delete_flag='1', update_time=now() where id=#{id}")
    int deleteProcessReport(@Param("id") Long id);

    @Select("select q.id, q.order_id orderId, o.order_no orderNo, o.material_name materialName, " +
            "q.inspector_name inspectorName, q.good_quantity goodQuantity, q.defect_quantity defectQuantity, " +
            "q.scrap_quantity scrapQuantity, q.defect_item defectItem, q.inspect_time inspectTime, q.remark, " +
            "q.create_time createTime, q.update_time updateTime, q.creator, q.tenant_id tenantId, q.delete_flag deleteFlag " +
            "from jsh_production_quality_inspection q left join jsh_production_order o on q.order_id = o.id " +
            "where ifnull(q.delete_flag,'0') != '1' and (#{tenantId} is null or q.tenant_id = #{tenantId}) " +
            "and (#{orderId} is null or q.order_id = #{orderId}) " +
            "and (#{keyword} is null or #{keyword} = '' or o.order_no like concat('%', #{keyword}, '%') " +
            "or o.material_name like concat('%', #{keyword}, '%') or q.inspector_name like concat('%', #{keyword}, '%') " +
            "or q.defect_item like concat('%', #{keyword}, '%')) order by q.id desc")
    List<ProductionQualityInspection> selectQualityInspectionList(@Param("keyword") String keyword,
                                                                  @Param("orderId") Long orderId,
                                                                  @Param("tenantId") Long tenantId);

    @Select("select id, order_id orderId, inspector_name inspectorName, good_quantity goodQuantity, " +
            "defect_quantity defectQuantity, scrap_quantity scrapQuantity, defect_item defectItem, inspect_time inspectTime, " +
            "remark, create_time createTime, update_time updateTime, creator, tenant_id tenantId, delete_flag deleteFlag " +
            "from jsh_production_quality_inspection where ifnull(delete_flag,'0') != '1' and id=#{id}")
    ProductionQualityInspection selectQualityInspectionById(@Param("id") Long id);

    @Insert("insert into jsh_production_quality_inspection (order_id, inspector_name, good_quantity, defect_quantity, " +
            "scrap_quantity, defect_item, inspect_time, remark, create_time, update_time, creator, tenant_id, delete_flag) values " +
            "(#{inspection.orderId}, #{inspection.inspectorName}, #{inspection.goodQuantity}, #{inspection.defectQuantity}, " +
            "#{inspection.scrapQuantity}, #{inspection.defectItem}, #{inspection.inspectTime}, #{inspection.remark}, " +
            "#{inspection.createTime}, #{inspection.updateTime}, #{inspection.creator}, #{inspection.tenantId}, #{inspection.deleteFlag})")
    @Options(useGeneratedKeys = true, keyProperty = "inspection.id")
    int insertQualityInspection(@Param("inspection") ProductionQualityInspection inspection);

    @Update("update jsh_production_quality_inspection set order_id=#{inspection.orderId}, " +
            "inspector_name=#{inspection.inspectorName}, good_quantity=#{inspection.goodQuantity}, " +
            "defect_quantity=#{inspection.defectQuantity}, scrap_quantity=#{inspection.scrapQuantity}, " +
            "defect_item=#{inspection.defectItem}, inspect_time=#{inspection.inspectTime}, remark=#{inspection.remark}, " +
            "update_time=#{inspection.updateTime} where id=#{inspection.id}")
    int updateQualityInspection(@Param("inspection") ProductionQualityInspection inspection);

    @Update("update jsh_production_quality_inspection set delete_flag='1', update_time=now() where id=#{id}")
    int deleteQualityInspection(@Param("id") Long id);
}
