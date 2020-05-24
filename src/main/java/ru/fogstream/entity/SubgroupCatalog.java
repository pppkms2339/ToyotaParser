package ru.fogstream.entity;

import javax.persistence.*;

@Entity(name = "subgroup_catalog")
public class SubgroupCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subgroup_name")
    private String subgroupName;

    @Column(name = "subgroup_code")
    private String subgroupCode;

    private String picture;

    public SubgroupCatalog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubgroupName() {
        return subgroupName;
    }

    public void setSubgroupName(String subgroupName) {
        this.subgroupName = subgroupName;
    }

    public String getSubgroupCode() {
        return subgroupCode;
    }

    public void setSubgroupCode(String subgroupCode) {
        this.subgroupCode = subgroupCode;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
