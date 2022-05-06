package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand, Long> {
    @Insert("insert into tb_category_brand(category_id, brand_id) values(#{cid}, #{bid})")
    int insertCategoryBrand(@Param("cid")Long cid, @Param("bid")Long bid);
    @Select("select b.* from  tb_brand b INNER JOIN tb_category_brand c  on b.id\n" +
            "=c.brand_id where c.category_id= #{cid};")
    List<Brand> selectBrandByCategoryId(@Param("cid")Long cid);
}
