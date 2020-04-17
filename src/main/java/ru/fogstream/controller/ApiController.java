package ru.fogstream.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.fogstream.entity.BodyBrand;
import ru.fogstream.entity.CarModel;
import ru.fogstream.entity.Equipment;
import ru.fogstream.repository.EquipmentRepository;
import ru.fogstream.repository.ModelRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api")
public class ApiController {

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    EquipmentRepository equipmentRepository;

    @Value("${site.name}")
    private String site;

    @GetMapping("/getModels")
    @ResponseBody
    public List<ObjectForOutput> getAllCarModels() {
        Iterable<CarModel> iterable = modelRepository.findAll();
        List<ObjectForOutput> list = new ArrayList<>();
        for(CarModel carModel : iterable) {
            list.add(new ObjectForOutput(carModel.getId(), carModel.getModelName()));
        }
        return list;
    }

    @GetMapping("/getBodies")
    @ResponseBody
    public ResponseEntity<List<ObjectForOutput>> getBodiesByModelId(@RequestParam("modelId") Long id) {
        List<ObjectForOutput> list = new ArrayList<>();
        CarModel carModel = modelRepository.findById(id).orElse(null);
        if(carModel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        for(BodyBrand bodyBrand : carModel.getBodyBrands()) {
            list.add(new ObjectForOutput(bodyBrand.getId(), bodyBrand.getBodyName()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/getEquipmentByBodyNumber")
    @ResponseBody
    public ResponseEntity<Equipment> getEquipmentByBodyNumber(@RequestParam("bodyNumber") String param) {
        Equipment equipment = null;
        try {
            Document doc = Jsoup.connect(site + "/search_frame/?frame_no=" + param).get();
            Elements elements = doc.select(".red");
            if (elements.size() > 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                Element table = doc.select(".table").first();
                Elements rows = table.select("tr");
                String name = rows.get(2).select("td").get(1).text();
                equipment = equipmentRepository.findByEquipmentName(name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(equipment);
    }

    private class ObjectForOutput {
        private Long id;
        private String name;

        public ObjectForOutput(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
