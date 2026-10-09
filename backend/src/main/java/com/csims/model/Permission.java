package com.csims.model;

public class Permission {
    private Integer id;
    private String name;
    private String module;
    private String description;

    public Permission() {}

    public Permission(Integer id, String name, String module, String description) {
        this.id = id;
        this.name = name;
        this.module = module;
        this.description = description;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
