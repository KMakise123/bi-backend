package com.hjh.bibackend.model.query.chartQuery;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ChartUpdateRequest implements Serializable {

    private static final long serialVersionUID = 2556612774190213417L;

    /**
     * id
     */
    private Long id;

    /**
     * 创建用户id
     */
    private Long userid;

    /**
     * 账号
     */
    private String goal;

    /**
     * 图标名称
     */
    private String name;

    /**
     * 图表数据
     */
    private String chartdata;

    /**
     * 图表类型
     */
    private String charttype;

    /**
     * 生成的图表数据
     */
    private String genchart;

    /**
     * 生成的分析结论
     */
    private String genresult;

    /**
     * 是否删除
     */
    private Integer isdelete;
}
