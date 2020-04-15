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
import ru.fogstream.entity.CarModel;
import ru.fogstream.entity.Equipment;
import ru.fogstream.repository.ModelRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class ApiController {

    @Autowired
    ModelRepository modelRepository;

    @Value("${site.name}")
    private String site;

    @GetMapping("/getModels")
    @ResponseBody
    public Iterable<CarModel> getAllCarModels() {
        return modelRepository.findAll();
    }

    @GetMapping("/getModelNames")
    @ResponseBody
    public Map<String, String> getAllCarModelNames() {
        Iterable<CarModel> iterable = modelRepository.findAll();
        Map<String, String> answer = new HashMap<>();
        int i = 0;
        for (CarModel carModel : iterable) {
            answer.put(Integer.toString(i), carModel.getModelName());
            i++;
        }
        return answer;
    }

    @GetMapping("/getEquipmentByBodyNumber")
    @ResponseBody
    public ResponseEntity<Equipment> getEquipmentByBodyNumber(@RequestParam("bodyNumber") String param) {
        Equipment equipment = null;
        try {
            Document doc = Jsoup.connect(site + "/search_frame/?frame_no=" + param).get();
            Elements elements = doc.select(".red");
            if (elements.size() > 0) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            } else {
                equipment = new Equipment();
                Element table = doc.select(".table").first();
                Elements rows = table.select("tr");
                equipment.setPeriod(rows.get(0).select("td").get(1).text());
                equipment.setEquipmentName(rows.get(2).select("td").get(1).text());
                equipment.setEngine(rows.get(5).select("td").get(0).text());
                equipment.setBody(rows.get(5).select("td").get(1).text());
                equipment.setGrade(rows.get(5).select("td").get(2).text());
                equipment.setKpp(rows.get(5).select("td").get(3).text());
                equipment.setAnother(rows.get(5).select("td").get(4).text());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(equipment);
    }

}
