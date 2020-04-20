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
import org.springframework.web.bind.annotation.*;
import ru.fogstream.entity.*;
import ru.fogstream.repository.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api")
public class ApiController {

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    EquipmentRepository equipmentRepository;

    @Autowired
    BodyBrandRepository bodyBrandRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    SubgroupRepository subgroupRepository;

    @Autowired
    ComponentRepository componentRepository;

    @Value("${site.name}")
    private String site;

    @Value("${site.url}")
    private String appUrl;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/img/{fileName}")
    public void sendImage(@PathVariable("fileName") String fileName, HttpServletResponse response) throws IOException {
        byte[] array = Files.readAllBytes(Paths.get(uploadPath + fileName));
        response.getOutputStream().write(array);
        response.getOutputStream().close();
    }

    @GetMapping("/getModels")
    @ResponseBody
    public ResponseEntity<List<ObjectForOutput>> getAllCarModels() {
        Iterable<CarModel> iterable = modelRepository.findAll();
        if (iterable == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        List<ObjectForOutput> list = new ArrayList<>();
        for (CarModel carModel : iterable) {
            list.add(new ObjectForOutput(carModel.getId(), carModel.getModelName()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/getBodies")
    @ResponseBody
    public ResponseEntity<List<ObjectForOutput>> getBodiesByModelId(@RequestParam("modelId") Long id) {
        List<ObjectForOutput> list = new ArrayList<>();
        CarModel carModel = modelRepository.findById(id).orElse(null);
        if (carModel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        for (BodyBrand bodyBrand : carModel.getBodyBrands()) {
            list.add(new ObjectForOutput(bodyBrand.getId(), bodyBrand.getBodyName()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/getEquipments")
    @ResponseBody
    public ResponseEntity<List<EquipmentForOutput>> getEquipmentsByBodyId(@RequestParam("bodyId") Long id) {
        List<EquipmentForOutput> list = new ArrayList<>();
        BodyBrand bodyBrand = bodyBrandRepository.findById(id).orElse(null);
        if (bodyBrand == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        for (Equipment equipment : bodyBrand.getEquipments()) {
            EquipmentForOutput eq = new EquipmentForOutput();
            eq.setId(equipment.getId());
            eq.setName(equipment.getEquipmentName());
            eq.setPeriod(equipment.getPeriod());
            eq.setEngine(equipment.getEngine());
            eq.setBody(equipment.getBody());
            eq.setGrade(equipment.getGrade());
            eq.setKpp(equipment.getKpp());
            eq.setAdditionalInfo(equipment.getAnother());
            list.add(eq);
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/getGroups")
    @ResponseBody
    public ResponseEntity<List<ObjectForOutput>> getGroupsByEquipmentId(@RequestParam("equipmentId") Long id) {
        List<ObjectForOutput> list = new ArrayList<>();
        Equipment equipment = equipmentRepository.findById(id).orElse(null);
        if (equipment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        for (GroupComp group : equipment.getGroups()) {
            list.add(new ObjectForOutput(group.getId(), group.getGroupName()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/getSubgroups")
    @ResponseBody
    public ResponseEntity<List<ExtendObjectForOutput>> getSubgroupsByGroupId(@RequestParam("groupId") Long id, HttpServletRequest request) {
        List<ExtendObjectForOutput> list = new ArrayList<>();
        GroupComp group = groupRepository.findById(id).orElse(null);
        if (group == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        for (SubgroupComp subgroupComp : group.getSubgroups()) {
            ExtendObjectForOutput obj = new ExtendObjectForOutput();
            obj.setId(subgroupComp.getId());
            obj.setName(subgroupComp.getSubgroupName());
            obj.setCode(subgroupComp.getSubgroupCode());
            obj.setPicture(appUrl + "/api/img/" + subgroupComp.getPicture());
            list.add(obj);
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/getComponents")
    @ResponseBody
    public ResponseEntity<List<ExtendObjectForOutput>> getComponentsBySubgroupId(@RequestParam("subgroupId") Long id, HttpServletRequest request) {
        List<ExtendObjectForOutput> list = new ArrayList<>();
        SubgroupComp subgroupComp = subgroupRepository.findById(id).orElse(null);
        if (subgroupComp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        for (Component component : subgroupComp.getComponents()) {
            ExtendObjectForOutput obj = new ExtendObjectForOutput();
            obj.setId(component.getId());
            obj.setName(component.getComponentName());
            obj.setCode(component.getComponentCode());
            obj.setPicture(appUrl + "/api/img/" + component.getPicture());
            list.add(obj);
        }
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping("/getUnits")
    @ResponseBody
    public ResponseEntity<List<UnitForOutput>> getUnitsByComponentId(@RequestParam("componentId") Long id) {
        List<UnitForOutput> list = new ArrayList<>();
        Component component = componentRepository.findById(id).orElse(null);
        if (component == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        for (Unit unit : component.getUnits()) {
            UnitForOutput ufo = new UnitForOutput();
            ufo.setId(unit.getId());
            ufo.setName(unit.getUnitName());
            ufo.setPnc(unit.getPnc());
            ufo.setOem(unit.getOem());
            ufo.setCountForAuto(unit.getCountForAuto());
            ufo.setPeriod(unit.getPeriod());
            ufo.setApplicability(unit.getApplicability());
            ufo.setInorder(unit.getInorder());
            ufo.setInorderLink(unit.getInorderLink());
            ufo.setInstock(unit.getInstock());
            ufo.setInstockLink(unit.getInstockLink());
            list.add(ufo);
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

        public ObjectForOutput() {
        }

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

    private class EquipmentForOutput {
        private Long id;
        private String name;
        private String period;
        private String engine;
        private String body;
        private String grade;
        private String kpp;
        private String additionalInfo;

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

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public String getEngine() {
            return engine;
        }

        public void setEngine(String engine) {
            this.engine = engine;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getKpp() {
            return kpp;
        }

        public void setKpp(String kpp) {
            this.kpp = kpp;
        }

        public String getAdditionalInfo() {
            return additionalInfo;
        }

        public void setAdditionalInfo(String additionalInfo) {
            this.additionalInfo = additionalInfo;
        }
    }

    private class ExtendObjectForOutput extends ObjectForOutput {
        private String code;
        private String picture;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }
    }

    private class UnitForOutput {
        private Long id;
        private String name;
        private String countForAuto;
        private String period;
        private String inorder;
        private String inorderLink;
        private String instock;
        private String instockLink;
        private String applicability;
        private String pnc;
        private String oem;

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

        public String getInorderLink() {
            return inorderLink;
        }

        public void setInorderLink(String inorderLink) {
            this.inorderLink = inorderLink;
        }

        public String getInstock() {
            return instock;
        }

        public void setInstock(String instock) {
            this.instock = instock;
        }

        public String getInstockLink() {
            return instockLink;
        }

        public void setInstockLink(String instockLink) {
            this.instockLink = instockLink;
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

        public String getOem() {
            return oem;
        }

        public void setOem(String oem) {
            this.oem = oem;
        }
    }
}
