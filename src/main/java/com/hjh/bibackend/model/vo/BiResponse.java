package com.hjh.bibackend.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BiResponse implements Serializable {
    private static final long serialVersionUID = 3327652727435280357L;

    private String genChart;

    private String genResult;

    private Long chartId;

}
