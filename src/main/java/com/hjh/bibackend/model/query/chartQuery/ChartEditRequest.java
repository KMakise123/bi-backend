package com.hjh.bibackend.model.query.chartQuery;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChartEditRequest implements Serializable {
    private static final long serialVersionUID = 6866510638054104353L;

    private Long id;

    private String name;

    private String goal;

    private String chartData;

    private String chartType;
}
