package cn.toguide.initializer.wizard.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DependencyCategory {

    private String name;

    private String description;

    @JsonProperty("values")
    private List<Dependency> list;
}
