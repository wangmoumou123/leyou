package com.leyou.item.pojo;

import lombok.Data;
import org.springframework.data.annotation.Transient;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Data
@Table(name = "tb_spec_group")
public class SpecGroup {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long cid;
    private String name;
    @Transient
    private List<SpecParam> params;
}
