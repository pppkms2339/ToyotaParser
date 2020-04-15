package ru.fogstream.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "component")
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "component_name")
    private String componentName;

    @Column(name = "component_code")
    private String componentCode;

    private String picture;

    private String link;

    @OneToOne
    private SubgroupComp subgroupComp;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Unit> units = new ArrayList<>();

    public Component() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentCode() {
        return componentCode;
    }

    public void setComponentCode(String componentCode) {
        this.componentCode = componentCode;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public SubgroupComp getSubgroupComp() {
        return subgroupComp;
    }

    public void setSubgroupComp(SubgroupComp subgroupComp) {
        this.subgroupComp = subgroupComp;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }
}
