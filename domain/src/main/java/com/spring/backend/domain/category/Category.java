package com.spring.backend.domain.category;

import com.spring.backend.domain.shared.AggregateRoot;

public class Category extends AggregateRoot {

    private String name;
    private String note;
    private Boolean isActive;

    private Category() {}

    public static Category reconstitute(Long id, String name, String note, Boolean isActive) {
        Category c = new Category();
        c.id = id;
        c.name = name;
        c.note = note;
        c.isActive = isActive;
        return c;
    }

    public String getName()      { return name; }
    public String getNote()      { return note; }
    public Boolean getIsActive() { return isActive; }
}
