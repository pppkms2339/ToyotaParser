package ru.fogstream.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "subgroup_comp")
public class SubgroupComp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subgroup_name")
    private String subgroupName;

    @Column(name = "subgroup_code")
    private String subgroupCode;

    private String picture;

    private String link;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Component> components = new ArrayList<>();

    public SubgroupComp() {
    }

    public Long getId() {
        return id;
    }

    public String getSubgroupName() {
        return subgroupName;
    }

    public String getSubgroupCode() {
        return subgroupCode;
    }

    public String getPicture() {
        return picture;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSubgroupName(String subgroupName) {
        this.subgroupName = subgroupName;
    }

    public void setSubgroupCode(String subgroupCode) {
        this.subgroupCode = subgroupCode;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
