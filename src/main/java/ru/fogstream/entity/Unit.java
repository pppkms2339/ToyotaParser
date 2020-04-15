package ru.fogstream.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "unit")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "count_for_auto")
    private String countForAuto;

    private String period;

    private String inorder;

    @Column(name = "inorder_link")
    private String inorderLink;

    private String instock;

    @Column(name = "instock_link")
    private String instockLink;

    private String applicability;

    private String pnc;

    private String oem;

    public Unit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPnc() {
        return pnc;
    }

    public void setPnc(String pnc) {
        this.pnc = pnc;
    }

    public String getInorderLink() {
        return inorderLink;
    }

    public void setInorderLink(String inorderLink) {
        this.inorderLink = inorderLink;
    }

    public String getInstockLink() {
        return instockLink;
    }

    public void setInstockLink(String instockLink) {
        this.instockLink = instockLink;
    }

    public String getOem() {
        return oem;
    }

    public void setOem(String oem) {
        this.oem = oem;
    }
}
