package ru.fogstream.controller;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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
import org.springframework.web.bind.annotation.ResponseBody;
import ru.fogstream.entity.*;
import ru.fogstream.repository.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

@Controller
@RequestMapping("/parse")
public class ParseController {

    private static final int REQUEST_COUNT = 25;
    private int count = 0, proxyCount = 0;
    private List<ProxyAddr> proxyAddrList = null;

    @Value("${site.name}")
    private String site;

    @Value("${proxy.url}")
    private String url;

    @Value("${upload.path}")
    private String uploadPath;

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

    @GetMapping("/ini")
    @ResponseBody
    private ResponseEntity<String> ini() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/ini", ""));
        proxyAddrList = getProxyListFromSite();
        errorRepository.save(new ToyotaError("End", new Date(), "parse/ini", ""));
        return ResponseEntity.status(HttpStatus.OK).body("ini done ok");
    }

    @GetMapping("/getModels")
    @ResponseBody
    private ResponseEntity<String> getCarModelForToyota() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getModels", ""));
        String pageHtml = getPageHtml(site);
        Document doc = Jsoup.parse(pageHtml);
        Elements elements = doc.select(".category2 li h4 a");
        for (Element element : elements) {
            CarModel carModel = new CarModel(element.text(), element.attr("href"));
            modelRepository.save(carModel);
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getModels", ""));
        return ResponseEntity.status(HttpStatus.OK).body("getCarModelForToyota done ok");
    }

    @GetMapping("/getBodies")
    @ResponseBody
    private ResponseEntity<String> getBodyBrandList() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getBodies", ""));
        Iterable<CarModel> models = modelRepository.findAll();
        for (CarModel carModel : models) {
            try {
                String pageHtml = getPageHtml(site + carModel.getLink());
                Document doc = Jsoup.parse(pageHtml);
                Elements elements = doc.select(".category2 li h4 a");
                for (Element element : elements) {
                    BodyBrand bodyBrand = new BodyBrand(element.text(), element.attr("href"));
                    bodyBrandRepository.save(bodyBrand);
                }
            } catch (Exception e) {
                errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getBodyBrandList", carModel.getModelName()));
                e.printStackTrace();
            }
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getBodies", ""));
        return ResponseEntity.status(HttpStatus.OK).body("getBodyBrandList done ok");
    }

    @GetMapping("/getEquipments")
    @ResponseBody
    private ResponseEntity<String> getEquipmentList() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getEquipments", ""));
        Iterable<BodyBrand> bodies = bodyBrandRepository.findAll();
        for (BodyBrand bodyBrand : bodies) {
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
                    equipmentRepository.save(equipment);
                }
            } catch (Exception e) {
                errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getEquipmentList", bodyBrand.getBodyName()));
                e.printStackTrace();
            }
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getEquipments", ""));
        return ResponseEntity.status(HttpStatus.OK).body("getEquipmentList done ok");
    }

    @GetMapping("/getGroups")
    @ResponseBody
    private ResponseEntity<String> getGroupList() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getGroups", ""));
        Iterable<Equipment> equipments = equipmentRepository.findAll();
        for (Equipment equipment : equipments) {
            try {
                String pageHtml = getPageHtml(site + equipment.getLink());
                Document doc = Jsoup.parse(pageHtml);
                Elements elements = doc.select("h3");
                for (int i = 0; i < elements.size() - 1; i++) {
                    String groupName = elements.get(i).text();
                    String link = elements.get(i).select("a").attr("href");
                    groupRepository.save(new GroupComp(groupName, link));
                }
            } catch (Exception e) {
                errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getGroupList", equipment.getEquipmentName()));
                e.printStackTrace();
            }
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getGroups", ""));
        return ResponseEntity.status(HttpStatus.OK).body("getGroupList done ok");
    }

    @GetMapping("/getSubgroups")
    @ResponseBody
    private ResponseEntity<String> getSubgroupList() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getSubgroups", ""));
        Iterable<GroupComp> groups = groupRepository.findAll();
        for (GroupComp groupComp : groups) {
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
                    URL url = new URL(site + src);
                    BufferedImage image = ImageIO.read(url);
                    if (image == null) {
                        errorRepository.save(new ToyotaError("image = null", new Date(), "getSubgroupList", groupComp.getGroupName()));
                        subgroup.setPicture("nophoto.jpg");
                    } else {
                        ImageIO.write(image, "png", new File(uploadPath + randomString + " " + fileName));
                    }
                    subgroupRepository.save(subgroup);
                }
            } catch (Exception e) {
                errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getSubgroupList", groupComp.getGroupName()));
                e.printStackTrace();
            }
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getSubgroups", ""));
        return ResponseEntity.status(HttpStatus.OK).body("getSubgroupList done ok");
    }

    @GetMapping("/getComponents")
    @ResponseBody
    private ResponseEntity<String> getComponentList() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getComponents", ""));
        Iterable<SubgroupComp> subgroups = subgroupRepository.findAll();
        for (SubgroupComp subgroup : subgroups) {
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
                        errorRepository.save(new ToyotaError("image = null", new Date(), "getComponentList", subgroup.getSubgroupName()));
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
                        componentRepository.save(component);
                    }
                }
            } catch (Exception e) {
                errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getComponentList", subgroup.getSubgroupName()));
                e.printStackTrace();
            }
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getComponents", ""));
        return ResponseEntity.status(HttpStatus.OK).body("getComponentList done ok");
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
}
