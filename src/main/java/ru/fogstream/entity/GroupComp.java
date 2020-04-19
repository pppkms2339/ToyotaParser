package ru.fogstream.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "group_comp")
public class GroupComp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name")
    private String groupName;

    private String link;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubgroupComp> subgroups = new ArrayList<>();

    public GroupComp() {
    }

    public GroupComp(String groupName, String link) {
        this.groupName = groupName;
        this.link = link;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<SubgroupComp> getSubgroups() {
        return subgroups;
    }

    public void setSubgroups(List<SubgroupComp> subgroups) {
        this.subgroups = subgroups;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
