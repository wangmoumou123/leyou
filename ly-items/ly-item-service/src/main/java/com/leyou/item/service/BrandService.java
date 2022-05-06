package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.xml.soap.Name;
import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page, rows);
        // 过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)) {
            example.createCriteria().orLike("name", key).orLike("letter", key);

        }
        //排序
        if (StringUtils.isNotBlank(sortBy)) {

            example.setOrderByClause(sortBy + (desc ? " DESC" : " ASC"));
        }
        //查询
        List<Brand> brands = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        // 解析分页结果
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());

    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        int insert = brandMapper.insert(brand);
        if (insert != 1) {
            throw new LyException(ExceptionEnum.BRAND_CREATE_ERROR);
        }

        for (Long cid : cids) {
            int insertCategoryBrand = brandMapper.insertCategoryBrand(cid, brand.getId());
            if (insertCategoryBrand != 1) {
                throw new LyException(ExceptionEnum.BRAND_CREATE_ERROR);
            }
        }

    }

    public Brand queryBtandById(Long brandId) {
        Brand brand = brandMapper.selectByPrimaryKey(brandId);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid) {

        List<Brand> brands = brandMapper.selectBrandByCategoryId(cid);
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        return brands;
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        return brands;
    }
}
