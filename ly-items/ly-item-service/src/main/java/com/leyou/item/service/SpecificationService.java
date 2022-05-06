package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup g = new SpecGroup();
        g.setCid(cid);
        List<SpecGroup> groupList = specGroupMapper.select(g);
        if (CollectionUtils.isEmpty(groupList)) {
            throw new LyException(ExceptionEnum.FIND_GROUP_BY_CID_NONE);
        }
        return groupList;
    }

    @Transactional
    public void addSpec(SpecGroup group) {

        List<SpecGroup> select = specGroupMapper.select(group);
        if (!CollectionUtils.isEmpty(select)) {
            throw new LyException(ExceptionEnum.HAS_BEEN_EXIST);
        }
        specGroupMapper.insert(group);
    }

    @Transactional
    public void editSpec(SpecGroup specGroup) {
        specGroupMapper.updateByPrimaryKeySelective(specGroup);
    }

    @Transactional
    public void deleteSpec(Long id) {

        specGroupMapper.deleteByPrimaryKey(id);
    }

    public List<SpecParam> querySpecParamsByGid(Long gid, Long cid, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        List<SpecParam> select = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(select)) {
            throw new LyException(ExceptionEnum.QUERY_SPECPARAMS_BY_ID_IS_NULL);
        }
        return select;
    }

    @Transactional
    public void saveSpecParam(SpecParam specParam) {
        // 检验参数
        if (specParam.getName() == null) {
            throw new LyException(ExceptionEnum.NAME_ERROR);
        }
        specParamMapper.insertSelective(specParam);

    }

    @Transactional
    public void delSpecParam(Long id) {
        specParamMapper.deleteByPrimaryKey(id);
    }

    @Transactional
    public void editSpecParam(SpecParam specParam) {
        specParamMapper.updateByPrimaryKeySelective(specParam);
    }

    public List<SpecParam> querySpecParamByCid(Long cid) {
        SpecParam specParam = new SpecParam();
        specParam.setCid(cid);
        List<SpecParam> specParamList = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(specParamList)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOND);
        }
        return specParamList;
    }

    public List<SpecGroup> queryListByCid(Long cid) {
        //查询 specGroups
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        // 查询参数
        List<SpecParam> specParams = querySpecParamsByGid(null, cid, null);
        //封装
        Map<Long, List<SpecParam>> map = new HashMap<>();
        for (SpecParam specParam : specParams) {
            if (!map.containsKey(specParam.getGroupId())) {
                map.put(specParam.getGroupId(), new ArrayList<>());
            }
            //填充  specParam
            map.get(specParam.getGroupId()).add(specParam);
        }

        // 填充到 specGroup
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }

        return specGroups;
    }
}
