package com.hjh.bibackend.model.query.userQuery;

import com.hjh.bibackend.model.query.PageQuery;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserSelectQuery{
    @NotNull(message = "参数不能为空")
    private PageQuery pageQuery;

    private String account;

    private String username;

    private String userRole;
}
