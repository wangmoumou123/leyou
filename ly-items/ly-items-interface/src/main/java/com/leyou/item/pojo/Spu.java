package com.leyou.item.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Table(name = "tb_spu")
@Data
public class Spu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long brandId;
    private Long cid1;// 1级类目
    private Long cid2;// 2级类目
    private Long cid3;// 3级类目
    private String title;// 标题
    private String subTitle;// 子标题

    @JsonIgnore  // 不从数据库返回值
    private Boolean saleable;// 是否上架
    private Boolean valid;// 是否有效，逻辑删除用
    private Date createTime;// 创建时间

    @JsonIgnore
    private Date lastUpdateTime;// 最后修改时间
    // 省略getter和setter 由Data注解提供

    @Transient  //不被数据库读取为表字段
    private String cname;
    @Transient
    private String bname;
    //为了能正确解析前端请求发出的参数
    @Transient
    private List<Sku> skus;
    @Transient
    private SpuDetail spuDetail;
}