package com.jsh.erp.datasource.mappers;

import com.jsh.erp.datasource.entities.ProductionOrder;
import com.jsh.erp.datasource.entities.ProductionOrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    @Insert("insert into jsh_production_order (order_no, bom_id, material_id, material_extend_id, material_name, material_unit, " +
            "plan_quantity, finished_quantity, plan_start_date, plan_finish_date, status, remark, create_time, update_time, creator, delete_flag) values " +
            "(#{order.orderNo}, #{order.bomId}, #{order.materialId}, #{order.materialExtendId}, #{order.materialName}, #{order.materialUnit}, " +
            "#{order.planQuantity}, #{order.finishedQuantity}, #{order.planStartDate}, #{order.planFinishDate}, #{order.status}, #{order.remark}, " +
            "#{order.createTime}, #{order.updateTime}, #{order.creator}, #{order.deleteFlag})")
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

    @Select("select id, order_id orderId, bom_item_id bomItemId, material_id materialId, material_extend_id materialExtendId, " +
            "material_name materialName, material_unit materialUnit, planned_quantity plannedQuantity, issued_quantity issuedQuantity, " +
            "remark, sort, tenant_id tenantId, delete_flag deleteFlag from jsh_production_order_item " +
            "where ifnull(delete_flag,'0') != '1' and order_id=#{orderId} order by sort asc, id asc")
    List<ProductionOrderItem> selectOrderItems(@Param("orderId") Long orderId);

    @Insert("insert into jsh_production_order_item (order_id, bom_item_id, material_id, material_extend_id, material_name, material_unit, " +
            "planned_quantity, issued_quantity, remark, sort, delete_flag) values (#{item.orderId}, #{item.bomItemId}, " +
            "#{item.materialId}, #{item.materialExtendId}, #{item.materialName}, #{item.materialUnit}, #{item.plannedQuantity}, " +
            "#{item.issuedQuantity}, #{item.remark}, #{item.sort}, #{item.deleteFlag})")
    int insertOrderItem(@Param("item") ProductionOrderItem item);

    @Update("update jsh_production_order_item set delete_flag='1' where order_id=#{orderId}")
    int deleteOrderItems(@Param("orderId") Long orderId);
}
