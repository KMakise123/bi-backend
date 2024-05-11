package com.hjh.bibackend.model.enums;

import lombok.Getter;

@Getter
public enum StatusEnums {
    SUCCESS("success"),WAIT("wait"),RUNNING("running"),FAILED("failed");

    private String value;

    StatusEnums(String value) {
        this.value = value;
    }
}
