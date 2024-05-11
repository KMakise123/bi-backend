package com.hjh.bibackend.model.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 7652909808399002171L;

    Long id;
}
