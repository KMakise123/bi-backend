package com.hjh.bibackend.model.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PageQuery {

    @NotNull(message = "参数缺失")
    protected Long currentPage;

    @NotNull(message = "参数缺失")
    protected Long pageSize;
}
