package com.spring.backend.domain.product;

public class Image {

    private Long id;
    private String fileName;

    public Image(Long id, String fileName) {
        this.id = id;
        this.fileName = fileName;
    }

    public Long getId()       { return id; }
    public String getFileName() { return fileName; }
}
