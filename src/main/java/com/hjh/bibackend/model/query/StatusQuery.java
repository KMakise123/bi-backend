package com.hjh.bibackend.model.query;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class StatusQuery implements Serializable {
    private static final long serialVersionUID = -7236040144285916584L;

    @NotNull(message = "请求参数为空")
    private Long id;

    @NotNull(message = "请求参数为空")
    private Integer status;
}
