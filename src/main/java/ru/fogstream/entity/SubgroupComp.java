package ru.fogstream.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "subgroup_comp")
public class SubgroupComp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String link;

    @OneToOne
    @JoinColumn(name = "subgroup_catalog_id")
    private SubgroupCatalog subgroupCatalog;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Component> components = new ArrayList<>();

    public SubgroupComp() {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SubgroupCatalog getSubgroupCatalog() {
        return subgroupCatalog;
    }

    public void setSubgroupCatalog(SubgroupCatalog subgroupCatalog) {
        this.subgroupCatalog = subgroupCatalog;
    }
}
