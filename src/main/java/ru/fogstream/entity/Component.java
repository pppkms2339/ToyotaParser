package ru.fogstream.entity;

import javax.persistence.*;

@Entity(name = "component")
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "component_name")
    private String componentName;

    @Column(name = "component_code")
    private String componentCode;

    private String oem;

    @Column(name = "count_for_auto")
    private String countForAuto;

    private String period;

    private String inorder;

    private String instock;

    private String applicability;

    private String picture;

    private String link;

    @OneToOne
    private SubgroupComp subgroupComp;

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

    public String getOem() {
        return oem;
    }

    public void setOem(String oem) {
        this.oem = oem;
    }

    public String getCountForAuto() {
        return countForAuto;
    }

    public void setCountForAuto(String countForAuto) {
        this.countForAuto = countForAuto;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getInorder() {
        return inorder;
    }

    public void setInorder(String inorder) {
        this.inorder = inorder;
    }

    public String getInstock() {
        return instock;
    }

    public void setInstock(String instock) {
        this.instock = instock;
    }

    public String getApplicability() {
        return applicability;
    }

    public void setApplicability(String applicability) {
        this.applicability = applicability;
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
}
