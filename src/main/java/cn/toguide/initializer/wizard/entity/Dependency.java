package cn.toguide.initializer.wizard.entity;

import lombok.Data;

@Data
public class Dependency {

    private String id;

    private String name;

    private String description;

    private boolean selected = false;
}
