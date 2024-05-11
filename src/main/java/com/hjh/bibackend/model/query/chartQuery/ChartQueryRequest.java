package com.hjh.bibackend.model.query.chartQuery;

import com.hjh.bibackend.model.query.PageQuery;
import lombok.Data;
import org.springframework.data.domain.PageRequest;

import java.io.Serializable;

@Data
public class ChartQueryRequest extends PageQuery implements Serializable{
    private static final long serialVersionUID = -8288998282015907051L;

    private Long id;

    private String name;

    private String goal;

    private String chartType;

    private Long userId;
}
