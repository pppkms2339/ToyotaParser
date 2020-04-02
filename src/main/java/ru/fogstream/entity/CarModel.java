package ru.fogstream.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "model")
public class CarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name")
    private String modelName;

    private String link;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BodyBrand> bodyBrands = new ArrayList<>();

    public CarModel() {
    }

    public CarModel(String modelName, String link) {
        this.modelName = modelName;
        this.link = link;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<BodyBrand> getBodyBrands() {
        return bodyBrands;
    }

    public void setBodyBrands(List<BodyBrand> bodyBrands) {
        this.bodyBrands = bodyBrands;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}