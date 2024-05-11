package com.hjh.bibackend.model.query.userQuery;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PwdQuery implements Serializable {
    private static final long serialVersionUID = -8770175641467614068L;

    @NotNull(message = "请求参数为空")
    private Long id;

    @NotBlank(message = "请求参数为空")
    private String password;
}
