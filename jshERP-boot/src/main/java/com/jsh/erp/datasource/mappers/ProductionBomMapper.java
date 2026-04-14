package com.jsh.erp.datasource.mappers;

import com.jsh.erp.datasource.entities.ProductionBom;
import com.jsh.erp.datasource.entities.ProductionBomItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ProductionBomMapper {
    @Select("select id, bom_no bomNo, name, material_id materialId, material_extend_id materialExtendId, " +
            "material_name materialName, material_unit materialUnit, output_quantity outputQuantity, enabled, remark, " +
            "create_time createTime, update_time updateTime, creator, tenant_id tenantId, delete_flag deleteFlag " +
            "from jsh_production_bom where ifnull(delete_flag,'0') != '1' " +
            "and (#{tenantId} is null or tenant_id = #{tenantId}) " +
            "and (#{keyword} is null or #{keyword} = '' or name like concat('%', #{keyword}, '%') or bom_no like concat('%', #{keyword}, '%') or material_name like concat('%', #{keyword}, '%')) " +
            "order by id desc")
    List<ProductionBom> selectBomList(@Param("keyword") String keyword, @Param("tenantId") Long tenantId);

    @Select("select id, bom_no bomNo, name, material_id materialId, material_extend_id materialExtendId, " +
            "material_name materialName, material_unit materialUnit, output_quantity outputQuantity, enabled, remark, " +
            "create_time createTime, update_time updateTime, creator, tenant_id tenantId, delete_flag deleteFlag " +
            "from jsh_production_bom where ifnull(delete_flag,'0') != '1' and id = #{id}")
    ProductionBom selectBomById(@Param("id") Long id);

    @Select("select id, bom_no bomNo, name, material_id materialId, material_extend_id materialExtendId, " +
            "material_name materialName, material_unit materialUnit, output_quantity outputQuantity, enabled, remark, " +
            "create_time createTime, update_time updateTime, creator, tenant_id tenantId, delete_flag deleteFlag " +
            "from jsh_production_bom where ifnull(delete_flag,'0') != '1' and enabled = 1 " +
            "and (#{tenantId} is null or tenant_id = #{tenantId}) order by id desc")
    List<ProductionBom> selectEnabledBomList(@Param("tenantId") Long tenantId);

    @Insert("insert into jsh_production_bom (bom_no, name, material_id, material_extend_id, material_name, material_unit, " +
            "output_quantity, enabled, remark, create_time, update_time, creator, tenant_id, delete_flag) values " +
            "(#{bom.bomNo}, #{bom.name}, #{bom.materialId}, #{bom.materialExtendId}, #{bom.materialName}, #{bom.materialUnit}, " +
            "#{bom.outputQuantity}, #{bom.enabled}, #{bom.remark}, #{bom.createTime}, #{bom.updateTime}, #{bom.creator}, #{bom.tenantId}, #{bom.deleteFlag})")
    @Options(useGeneratedKeys = true, keyProperty = "bom.id")
    int insertBom(@Param("bom") ProductionBom bom);

    @Update("update jsh_production_bom set bom_no=#{bom.bomNo}, name=#{bom.name}, material_id=#{bom.materialId}, " +
            "material_extend_id=#{bom.materialExtendId}, material_name=#{bom.materialName}, material_unit=#{bom.materialUnit}, " +
            "output_quantity=#{bom.outputQuantity}, enabled=#{bom.enabled}, remark=#{bom.remark}, update_time=#{bom.updateTime} where id=#{bom.id}")
    int updateBom(@Param("bom") ProductionBom bom);

    @Update("update jsh_production_bom set delete_flag='1', update_time=now() where id=#{id}")
    int deleteBom(@Param("id") Long id);

    @Select("select id, bom_id bomId, material_id materialId, material_extend_id materialExtendId, material_name materialName, " +
            "material_unit materialUnit, quantity, loss_rate lossRate, remark, sort, tenant_id tenantId, delete_flag deleteFlag " +
            "from jsh_production_bom_item where ifnull(delete_flag,'0') != '1' and bom_id=#{bomId} order by sort asc, id asc")
    List<ProductionBomItem> selectBomItems(@Param("bomId") Long bomId);

    @Insert("insert into jsh_production_bom_item (bom_id, material_id, material_extend_id, material_name, material_unit, quantity, " +
            "loss_rate, remark, sort, tenant_id, delete_flag) values (#{item.bomId}, #{item.materialId}, #{item.materialExtendId}, " +
            "#{item.materialName}, #{item.materialUnit}, #{item.quantity}, #{item.lossRate}, #{item.remark}, #{item.sort}, #{item.tenantId}, #{item.deleteFlag})")
    int insertBomItem(@Param("item") ProductionBomItem item);

    @Update("update jsh_production_bom_item set delete_flag='1' where bom_id=#{bomId}")
    int deleteBomItems(@Param("bomId") Long bomId);

    @Delete("delete from jsh_production_bom_item where ifnull(delete_flag,'0') = '1' and bom_id=#{bomId}")
    int purgeDeletedBomItems(@Param("bomId") Long bomId);
}
