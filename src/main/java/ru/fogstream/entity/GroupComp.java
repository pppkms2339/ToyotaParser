package ru.fogstream.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "group_comp")
public class GroupComp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String link;

    @OneToOne
    @JoinColumn(name = "group_catalog_id")
    private GroupCatalog groupCatalog;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubgroupComp> subgroups = new ArrayList<>();

    public GroupComp() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public GroupCatalog getGroupCatalog() {
        return groupCatalog;
    }

    public void setGroupCatalog(GroupCatalog groupCatalog) {
        this.groupCatalog = groupCatalog;
    }
}
