package ru.fogstream.entity;

import javax.persistence.*;

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

    @OneToOne
    private GroupComp groupComp;

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

    public GroupComp getGroupComp() {
        return groupComp;
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

    public void setGroupComp(GroupComp groupComp) {
        this.groupComp = groupComp;
    }
}
