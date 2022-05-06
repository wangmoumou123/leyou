package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    PRICE_CAN_NOT_NULL(400, "价格不能为空"),
    CATEGORY_NOT_FOND(404, "商品分类没查到"),
    BRAND_NOT_FOND(404, "商品品牌没有查到"),
    BRAND_CREATE_ERROR(500, "品牌添加失败"),
    UPLOAD_FILE_ERROR(500, "上传文件失败"),
    INVALID_FILE_TYPE(404, "文件类型失败"),
    FIND_GROUP_BY_CID_NONE(500, "查找spec group失败"),
    HAS_BEEN_EXIST(500, "已存在"),
    QUERY_SPECPARAMS_BY_ID_IS_NULL(500, "查询spec_params结果为null"),
    NAME_ERROR(400, "name为空"),
    GOODS_NOT_FOND(404, "商品未找到"),
    CATEGORIES_NOT_FOND(404, "分类名称没找到"),
    SPEC_PARAM_NOT_FOND(404, "spec_params 未找到"),
    GOODS_SAVE_ERROR(404, "商品保存失败"),
    SpuDetail_NOT_FOND(404, "商品详情未找到"),
    SKUS_NOT_FOND(404, "skus 查找失败"),
    SPUS_NOT_FIND(404, "spus未找到"),
    INVALID_USER_DATATYPE_ERROR(400, "用户数据异常")
    ;
    private int code;
    private String message;

}
