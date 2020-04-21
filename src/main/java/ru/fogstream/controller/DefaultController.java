package ru.fogstream.controller;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.fogstream.entity.*;
import ru.fogstream.repository.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

@Controller
public class DefaultController {

    private static final int REQUEST_COUNT = 25;
    private int count = 0, proxyCount = 0;
    private List<ProxyAddr> proxyAddrList = null;

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    BodyBrandRepository bodyBrandRepository;

    @Autowired
    EquipmentRepository equipmentRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    SubgroupRepository subgroupRepository;

    @Autowired
    ComponentRepository componentRepository;

    @Autowired
    ErrorRepository errorRepository;

    @Value("${site.name}")
    private String site;

    @Value("${proxy.url}")
    private String url;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        Iterable<CarModel> carModels = modelRepository.findAll();
        ArrayList<CarModel> cars = new ArrayList<>();
        carModels.forEach(cars::add);
        model.addAttribute("cars", cars);
        return "index";
    }

    @GetMapping("/parse")
    @ResponseBody
    public void parse(Model model) throws IOException {
        modelRepository.deleteAll();
        proxyAddrList = getProxyListFromSite();
        long start = System.currentTimeMillis();
//        //Получение всех моделей
//        getCarModelForToyota();
//        //Получение кузовов для каждой модели
//        Iterable<CarModel> carModels = modelRepository.findAll();
//        for(CarModel carModel : carModels) {
//            List<BodyBrand> bodyBrands = getBodyBrandListForCar(carModel);
//            carModel.getBodyBrands().addAll(bodyBrands);
//            modelRepository.save(carModel);
//        }
//        //Получение комплектаций для каждого кузова
//        Iterable<BodyBrand> bodyBrands = bodyBrandRepository.findAll();
//        for(BodyBrand bodyBrand : bodyBrands) {
//            List<Equipment> equipments = getEquipmentListForBody(bodyBrand);
//            bodyBrand.getEquipments().addAll(equipments);
//            bodyBrandRepository.save(bodyBrand);
//        }
//        //Получение групп деталей для каждой комплектации
//        Iterable<Equipment> equipments = equipmentRepository.findAll();
//        for(Equipment equipment : equipments) {
//            List<GroupComp> groups = getGroupListForEquipment(equipment);
//            equipment.getGroups().addAll(groups);
//            equipmentRepository.save(equipment);
//        }
//        //Получение подгрупп деталей для каждой группы
//        Iterable<GroupComp> groups = groupRepository.findAll();
//        for(GroupComp groupComp : groups) {
//            List<SubgroupComp> subgroups = getSubgroupListForGroup(groupComp);
//            groupComp.getSubgroups().addAll(subgroups);
//            groupRepository.save(groupComp);
//        }
//        //Получение деталей для каждой подгруппы
//        Iterable<SubgroupComp> subgroups = subgroupRepository.findAll();
//        for(SubgroupComp subgroupComp : subgroups) {
//            List<Component> components = getComponentListForSubgroup(subgroupComp);
//            subgroupComp.getComponents().addAll(components);
//            subgroupRepository.save(subgroupComp);
//        }

        CarModel testCar = new CarModel("Allex", "/allex/");
        BodyBrand testBody = new BodyBrand("NZE121", "/allex/nze121/");
        testCar.getBodyBrands().add(testBody);
        Equipment testEquipment = new Equipment();
        testEquipment.setEquipmentName("NZE121-AHPNK");
        testEquipment.setLink("/allex/nze121/139267/");
        testBody.getEquipments().add(testEquipment);
        modelRepository.save(testCar);
        //Получение групп деталей для каждой комплектации
        Iterable<Equipment> equipments = equipmentRepository.findAll();
        for (Equipment equipment : equipments) {
            List<GroupComp> groups = getGroupListForEquipment(equipment);
            equipment.getGroups().addAll(groups);
            equipmentRepository.save(equipment);
        }
        //Получение подгрупп деталей для каждой группы
        Iterable<GroupComp> groups = groupRepository.findAll();
        for (GroupComp groupComp : groups) {
            List<SubgroupComp> subgroups = getSubgroupListForGroup(groupComp);
            groupComp.getSubgroups().addAll(subgroups);
            groupRepository.save(groupComp);
        }
        //Получение деталей для каждой подгруппы
        Iterable<SubgroupComp> subgroups = subgroupRepository.findAll();
        for (SubgroupComp subgroupComp : subgroups) {
            List<Component> components = getComponentListForSubgroup(subgroupComp);
            subgroupComp.getComponents().addAll(components);
            subgroupRepository.save(subgroupComp);
        }
        System.out.println("Время работы: " + (System.currentTimeMillis() - start) / 1000);
    }

    //Модели
    private void getCarModelForToyota() {
        String pageHtml = getPageHtml(site);
        Document doc = Jsoup.parse(pageHtml);
        if (pageHtml == null || pageHtml.equals("")) {
            errorRepository.save(new ToyotaError("pageHtml=null", new Date(), "getCarModelForToyota", ""));
            return;
        }
        Elements elements = doc.select(".category2 li h4 a");
        for (Element element : elements) {
            CarModel carModel = new CarModel(element.text(), element.attr("href"));
            modelRepository.save(carModel);
        }
    }

    //Марки кузовов
    private List<BodyBrand> getBodyBrandListForCar(CarModel carModel) {
        List<BodyBrand> bodyBrands = new ArrayList<>();
        try {
            String pageHtml = getPageHtml(site + carModel.getLink());
            Document doc = Jsoup.parse(pageHtml);
            Elements elements = doc.select(".category2 li h4 a");
            for (Element element : elements) {
                BodyBrand bodyBrand = new BodyBrand(element.text(), element.attr("href"));
                bodyBrands.add(bodyBrand);
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getBodyBrandListForCar", carModel.getModelName()));
            e.printStackTrace();
        }
        return bodyBrands;
    }

    //Комплектации
    private List<Equipment> getEquipmentListForBody(BodyBrand bodyBrand) {
        List<Equipment> equipments = new ArrayList<>();
        try {
            String pageHtml = getPageHtml(site + bodyBrand.getLink());
            Document doc = Jsoup.parse(pageHtml);
            Element table = doc.select(".table").first();
            Elements rows = table.select("tr");
            for (int i = 2; i < rows.size(); i++) {
                Equipment equipment = new Equipment();
                equipment.setLink(rows.get(i).select("td").get(0).select("a").attr("href"));
                equipment.setEquipmentName(rows.get(i).select("td").get(0).text());
                equipment.setEngine(rows.get(i).select("td").get(1).text());
                equipment.setPeriod(rows.get(i).select("td").get(2).text());
                equipment.setBody(rows.get(i).select("td").get(3).text());
                equipment.setGrade(rows.get(i).select("td").get(4).text());
                equipment.setKpp(rows.get(i).select("td").get(5).text());
                equipment.setAnother(rows.get(i).select("td").get(6).text());
                equipments.add(equipment);
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getEquipmentListForBody", bodyBrand.getBodyName()));
            e.printStackTrace();
        }
        return equipments;
    }

    private List<GroupComp> getGroupListForEquipment(Equipment equipment) {
        List<GroupComp> groups = new ArrayList<>();
        try {
            String pageHtml = getPageHtml(site + equipment.getLink());
            Document doc = Jsoup.parse(pageHtml);
            Elements elements = doc.select("h3");
            for (int i = 0; i < elements.size() - 1; i++) {
                String groupName = elements.get(i).text();
                String link = elements.get(i).select("a").attr("href");
                groups.add(new GroupComp(groupName, link));
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getGroupListForEquipment", equipment.getEquipmentName()));
            e.printStackTrace();
        }
        return groups;
    }

    private List<SubgroupComp> getSubgroupListForGroup(GroupComp groupComp) {
        List<SubgroupComp> subgroups = new ArrayList<>();
        try {
            String pageHtml = getPageHtml(site + groupComp.getLink());
            Document doc = Jsoup.parse(pageHtml);
            Elements elements = doc.select(".parts_picture");
            for (Element element : elements) {
                SubgroupComp subgroup = new SubgroupComp();
                subgroup.setSubgroupName(element.attr("title"));
                subgroup.setLink(element.parent().attr("href"));
                subgroup.setSubgroupCode(element.parent().parent().text().substring(0, 5));
                //копируем картинку на диск
                String src = element.attr("src");
                String fileName = src.substring(src.lastIndexOf('/') + 1);
                String randomString = UUID.randomUUID().toString();
                subgroup.setPicture(randomString + " " + fileName);
                subgroups.add(subgroup);
                URL url = new URL(site + src);
                BufferedImage image = ImageIO.read(url);
                if (image == null) {
                    errorRepository.save(new ToyotaError("image = null", new Date(), "getSubgroupListForGroup", groupComp.getGroupName()));
                    subgroup.setPicture("nophoto.jpg");
                } else {
                    ImageIO.write(image, "png", new File(uploadPath + randomString + " " + fileName));
                }
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getSubgroupListForGroup", groupComp.getGroupName()));
            e.printStackTrace();
        }
        return subgroups;
    }

    private List<Component> getComponentListForSubgroup(SubgroupComp subgroup) {
        List<Component> components = new ArrayList<>();
        try {
            String pageHtml = getPageHtml(site + subgroup.getLink());
            Document doc = Jsoup.parse(pageHtml);
            Elements tables = doc.select("table");
            for (Element table : tables) {
                //копируем картинку на диск
                String src = table.select("#part_image img").attr("src");
                String fileName = src.substring(src.lastIndexOf('/') + 1);
                String randomString = UUID.randomUUID().toString();
                URL url = new URL(site + src);
                BufferedImage image = ImageIO.read(url);
                String fileNameForBase = null;
                if (image == null) {
                    errorRepository.save(new ToyotaError("image = null", new Date(), "getComponentListForSubgroup", subgroup.getSubgroupName()));
                    fileNameForBase = "nophoto.jpg";
                } else {
                    ImageIO.write(image, "png", new File(uploadPath + randomString + " " + fileName));
                    fileNameForBase = randomString + " " + fileName;
                }
                //получаем список деталей
                Elements elements = table.select(".detail-list a");
                for (Element element : elements) {
                    String text = element.text();
                    if (text.startsWith("**")) {
                        continue;
                    }
                    Component component = new Component();
                    component.setPicture(fileNameForBase); //картинка у всех одна
                    component.setComponentName(text);
                    String href = element.attr("href");
                    if (href.contains("?")) {
                        component.setComponentCode(href.substring(href.indexOf('=') + 1));
                    } else {
                        String[] hrefParts = href.split("/");
                        component.setComponentCode(hrefParts[hrefParts.length - 1]);
                    }
                    component.setLink(href);
                    getUnitListForComponent(component); //получим список unit для данной детали
                    components.add(component);
                }
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getComponentListForSubgroup", subgroup.getSubgroupName()));
            e.printStackTrace();
        }
        return components;
    }

    private void getUnitListForComponent(Component component) {
        List<Unit> units = new ArrayList<>();
        try {
            String pageHtml = getPageHtml(site + component.getLink());
            Document doc = Jsoup.parse(pageHtml);
            Elements rows = doc.select(".parts-in-stock-widget_part-row");
            for (Element row : rows) {
                Unit unit = new Unit();
                Elements tds = row.select("td");
                if (tds.size() == 9) {
                    unit.setPnc(tds.get(1).text());
                    unit.setOem(tds.get(2).text());
                    unit.setCountForAuto(tds.get(3).text());
                    unit.setPeriod(tds.get(4).text());
                    unit.setUnitName(tds.get(5).text());
                    unit.setApplicability(tds.get(6).text());
                    unit.setInorder(tds.get(7).select("a").text());
                    unit.setInorderLink(tds.get(7).select("a").attr("href"));
                    unit.setInstock(tds.get(8).select("a").text());
                    unit.setInstockLink(tds.get(8).select("a").attr("href"));
                } else {
                    unit.setPnc(tds.get(1).text());
                    unit.setOem(tds.get(2).text());
                    unit.setCountForAuto(tds.get(3).text());
                    unit.setInorder(tds.get(5).select("a").text());
                    unit.setInorderLink(tds.get(5).select("a").attr("href"));
                    unit.setInstock(tds.get(6).select("a").text());
                    unit.setInstockLink(tds.get(6).select("a").attr("href"));
                }
                units.add(unit);
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getUnitListForComponent", component.getComponentName()));
            e.printStackTrace();
        }
        component.getUnits().addAll(units);
    }

    @GetMapping("/model/{carModelId}")
    public String bodyBrand(@PathVariable("carModelId") Long id, Model model) {
        CarModel car = modelRepository.findById(id).get();
        model.addAttribute("car", car);
        List<BodyBrand> bodyBrands = car.getBodyBrands();
        model.addAttribute("bodyBrands", bodyBrands);
        return "bodyBrand";
    }

    @GetMapping("/model/{carModelId}/body/{bodyBrandId}")
    public String equipment(@PathVariable("carModelId") Long modelId, @PathVariable("bodyBrandId") Long bodyId, Model model) {
        CarModel car = modelRepository.findById(modelId).get();
        model.addAttribute("car", car);
        BodyBrand bodyBrand = bodyBrandRepository.findById(bodyId).get();
        model.addAttribute("body", bodyBrand);
        List<Equipment> equipments = bodyBrand.getEquipments();
        model.addAttribute("equipments", equipments);
        return "equipment";
    }

    @GetMapping("/model/{carModelId}/body/{bodyBrandId}/equipment/{equipmentId}")
    public String component(@PathVariable("carModelId") Long modelId, @PathVariable("bodyBrandId") Long bodyId, @PathVariable("equipmentId") Long equipmentId, Model model) {
        CarModel car = modelRepository.findById(modelId).get();
        model.addAttribute("car", car);
        BodyBrand bodyBrand = bodyBrandRepository.findById(bodyId).get();
        model.addAttribute("body", bodyBrand);
        Equipment equipment = equipmentRepository.findById(equipmentId).get();
        model.addAttribute("equipment", equipment);
        //List<Component> components = equipment.getComponents();
        //model.addAttribute("components", components);
        return "component";
    }

    @GetMapping("/model/{carModelId}/body/{bodyBrandId}/equipment/{equipmentId}/component/{componentId}")
    public String unit(@PathVariable("carModelId") Long modelId,
                       @PathVariable("bodyBrandId") Long bodyId,
                       @PathVariable("equipmentId") Long equipmentId,
                       @PathVariable("componentId") Long componentId,
                       Model model) {
        CarModel car = modelRepository.findById(modelId).get();
        model.addAttribute("car", car);
        BodyBrand bodyBrand = bodyBrandRepository.findById(bodyId).get();
        model.addAttribute("body", bodyBrand);
        Equipment equipment = equipmentRepository.findById(equipmentId).get();
        model.addAttribute("equipment", equipment);
        Component component = componentRepository.findById(componentId).get();
        model.addAttribute("component", component);
        List<Unit> units = component.getUnits();
        model.addAttribute("units", units);
        return "unit";
    }

    @GetMapping("/searchBodyNumber")
    public String searchBodyNumber(@RequestParam("param") String param, Model model) {
        try {
            Document doc = Jsoup.connect(site + "/search_frame/?frame_no=" + param).get();
            Elements elements = doc.select(".red");
            if (elements.size() > 0) {
                model.addAttribute("exist", false);
            } else {
                model.addAttribute("exist", true);
                elements = doc.select("h1");
                model.addAttribute("auto", elements.text());
                Element table = doc.select(".table").first();
                Elements rows = table.select("tr");
                model.addAttribute("period", rows.get(0).select("td").get(1).text());
                model.addAttribute("code", rows.get(1).select("td").get(1).text());
                model.addAttribute("equipment", rows.get(2).select("td").get(1).text());
                model.addAttribute("number", rows.get(3).select("td").get(1).text());
                model.addAttribute("engine", rows.get(5).select("td").get(0).text());
                model.addAttribute("body", rows.get(5).select("td").get(1).text());
                model.addAttribute("grade", rows.get(5).select("td").get(2).text());
                model.addAttribute("kpp", rows.get(5).select("td").get(3).text());
                model.addAttribute("another", rows.get(5).select("td").get(4).text());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "searchBodyNumber";
    }

    private List<ProxyAddr> getProxyListFromSite() {
        List<ProxyAddr> answer = new ArrayList<>();
        try {
            String json = Jsoup.connect(url).ignoreContentType(true).execute().body();
            JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(json);
            JSONObject list = (JSONObject) jsonObject.get("list");
            Set<String> keys = list.keySet();
            for (String key : keys) {
                JSONObject element = (JSONObject) list.get(key);
                ProxyAddr p = new ProxyAddr((String) element.get("ip"), Integer.parseInt(element.get("port").toString()), (String) element.get("user"), (String) element.get("pass"));
                answer.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return answer;
    }

    private String getPageHtml(String page) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            ProxyAddr currentProxy = proxyAddrList.get(proxyCount);
            String command = "curl -U " + currentProxy.getUser() + ":" + currentProxy.getPassword() + " -x " + currentProxy.getAddress() + ":" + currentProxy.getPort() + " " + page;
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            Process process = processBuilder.start();
            String line = null;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"))) {
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
            process.destroy();
            count++;
            if (count >= REQUEST_COUNT) {
                count = 0;
                proxyCount++;
                if (proxyCount >= proxyAddrList.size()) {
                    proxyCount = 0;
                }
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getPageHtml", page));
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
