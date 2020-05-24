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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.fogstream.entity.*;
import ru.fogstream.repository.*;
import ru.fogstream.utility.Mail;

import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
@RequestMapping("/parse")
public class ParseController {

    private static final int REQUEST_COUNT = 25;
    private int count = 0, proxyCount = 0;
    private List<ProxyAddr> proxyAddrList = null;
    private List<GroupCatalog> groupsFromBase = null;
    private List<SubgroupCatalog> subgroupsFromBase = null;

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

    @Autowired
    GroupCatalogRepository groupCatalogRepository;

    @Autowired
    SubgroupCatalogRepository subgroupCatalogRepository;

    @GetMapping("/ini")
    @ResponseBody
    public ResponseEntity<String> ini() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/ini", ""));
        proxyAddrList = getProxyListFromSite();
        Iterable<GroupCatalog> groupCatalog = groupCatalogRepository.findAll();
        groupsFromBase = new ArrayList<>();
        groupCatalog.forEach(groupsFromBase::add);
        Iterable<SubgroupCatalog> subgroupCatalog = subgroupCatalogRepository.findAll();
        subgroupsFromBase = new ArrayList<>();
        subgroupCatalog.forEach(subgroupsFromBase::add);
        errorRepository.save(new ToyotaError("End", new Date(), "parse/ini", proxyAddrList.size() + " addresses, " + groupsFromBase.size() + " groups, " + subgroupsFromBase.size() + " subgroups"));
        return ResponseEntity.status(HttpStatus.OK).body("ini done ok");
    }

    @GetMapping("/getModels")
    @ResponseBody
    public ResponseEntity<String> getCarModelForToyota() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getModels", ""));
        Document doc = getDocumentFromHtml(site);
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
    public ResponseEntity<String> getBodyBrandList() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getBodies", ""));
        Iterable<CarModel> models = modelRepository.findAll();
        for (CarModel carModel : models) {
            List<BodyBrand> bodies = new ArrayList<>();
            try {
                Document doc = getDocumentFromHtml(site + carModel.getLink());
                Elements elements = doc.select(".category2 li h4 a");
                for (Element element : elements) {
                    BodyBrand bodyBrand = new BodyBrand(element.text(), element.attr("href"));
                    bodies.add(bodyBrand);
                }
                carModel.getBodyBrands().addAll(bodies);
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
    public ResponseEntity<String> getEquipmentList() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getEquipments", ""));
        Iterable<BodyBrand> bodies = bodyBrandRepository.findAll();
        for (BodyBrand bodyBrand : bodies) {
            List<Equipment> equipments = new ArrayList<>();
            try {
                Document doc = getDocumentFromHtml(site + bodyBrand.getLink());
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
                bodyBrand.getEquipments().addAll(equipments);
            } catch (Exception e) {
                errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getEquipmentList", bodyBrand.getBodyName()));
                e.printStackTrace();
            }
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getEquipments", ""));
        return ResponseEntity.status(HttpStatus.OK).body("getEquipmentList done ok");
    }

    //Получение всей информации для всех моделей
    @GetMapping("/getAll")
    @ResponseBody
    public ResponseEntity<String> parseAll() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/parseAll", ""));
        Document doc = getDocumentFromHtml(site);
        Elements elements = doc.select(".category2 li h4 a");
        for (Element element : elements) {
            CarModel carModel = new CarModel(element.text(), element.attr("href"));
            //Получение кузовов для данной модели
            carModel.getBodyBrands().addAll(getBodiesForModel(carModel));
            modelRepository.save(carModel);
            try {
                //Отправляем письмо с указанием того, что очередная модель обработана
                new Thread(() -> {
                    new Mail().send(carModel.getModelName() + " загружена", carModel.getBodyBrands().size() + " марок кузовов");
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            errorRepository.save(new ToyotaError("Model " + carModel.getModelName(), new Date(), "parse/parseAll", ""));
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/parseAll", ""));
        return ResponseEntity.status(HttpStatus.OK).body("parseAll done ok");
    }

    //Получение всей информации для пропущенных моделей
    @GetMapping("/getAllSkip")
    @ResponseBody
    public ResponseEntity<String> parseAllSkip() {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/parseAllSkip", ""));
        Document doc = getDocumentFromHtml(site);
        Elements elements = doc.select(".category2 li h4 a");
        Iterable<CarModel> iterable = modelRepository.findAll();
        List<CarModel> modelsFromBase = new ArrayList<>();
        iterable.forEach(modelsFromBase::add);
        for (Element element : elements) {
            if (isExistModel(modelsFromBase, element.text(), element.attr("href"))) {
                continue;
            }
            CarModel carModel = new CarModel(element.text(), element.attr("href"));
            //Получение кузовов для данной модели
            carModel.getBodyBrands().addAll(getBodiesForModel(carModel));
            modelRepository.save(carModel);
            try {
                //Отправляем письмо с указанием того, что очередная модель обработана
                new Thread(() -> {
                    new Mail().send(carModel.getModelName() + " загружена", carModel.getBodyBrands().size() + " марок кузовов");
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            errorRepository.save(new ToyotaError("Model " + carModel.getModelName(), new Date(), "parse/parseAll", ""));
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/parseAllSkip", ""));
        return ResponseEntity.status(HttpStatus.OK).body("parseAll done ok");
    }

    private boolean isExistModel(List<CarModel> models, String modelName, String link) {
        for (CarModel model : models) {
            if (model.getModelName().equals(modelName) && model.getLink().equals(link)) {
                return true;
            }
        }
        return false;
    }

    //Получение всей пропущенной информации для конкретной модели
    @GetMapping("/getModelSkip")
    @ResponseBody
    public ResponseEntity<String> parseModelSkip(@RequestParam("modelName") String modelName) {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getModelSkip", modelName));
        Document doc = getDocumentFromHtml(site);
        Elements elements = doc.select(".category2 li h4 a");
        for (Element element : elements) {
            if (element.text().equals(modelName)) {
                //Нашли требуемую модель на странице
                CarModel carModel = new CarModel(element.text(), element.attr("href"));
                //Получение кузовов для данной модели
                carModel.getBodyBrands().addAll(getBodiesSkipForModel(carModel));
                modelRepository.save(carModel);
                try {
                    //Отправляем письмо с указанием того, что очередная модель обработана
                    new Thread(() -> {
                        new Mail().send(carModel.getModelName() + " загружена", carModel.getBodyBrands().size() + " марок кузовов");
                    }).start();
                } catch (Exception e) {
                    errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "parse/getModelSkip", modelName));
                    e.printStackTrace();
                }
                break;
            }
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getModelSkip", modelName));
        return ResponseEntity.status(HttpStatus.OK).body("getModelSkip done ok");
    }

    //Получение кузовов для данной модели
    private List<BodyBrand> getBodiesForModel(CarModel carModel) {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getBodiesForModel", carModel.getModelName()));
        List<BodyBrand> bodies = new ArrayList<>();
        try {
            Document doc = getDocumentFromHtml(site + carModel.getLink());
            Elements elements = doc.select(".category2 li h4 a");
            for (Element element : elements) {
                BodyBrand bodyBrand = new BodyBrand(element.text(), element.attr("href"));
                //Получение комплектаций для данного кузова
                bodyBrand.getEquipments().addAll(getEquipmentsForBody(bodyBrand));
//                bodyBrandRepository.save(bodyBrand);
                bodies.add(bodyBrand);
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getBodiesForModel", carModel.getModelName()));
            e.printStackTrace();
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getBodiesForModel", carModel.getModelName()));
        return bodies;
    }

    //Получение пропущенных кузовов для данной модели
    private List<BodyBrand> getBodiesSkipForModel(CarModel carModel) {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getBodiesSkipForModel", carModel.getModelName()));
        List<BodyBrand> bodies = new ArrayList<>();
        try {
            Document doc = getDocumentFromHtml(site + carModel.getLink());
            Elements elements = doc.select(".category2 li h4 a");
            //Получаем список всех кузовов из БД, возможно некоторые кузова уже сохранены
            Iterable<BodyBrand> bodiesFromBase = bodyBrandRepository.findAll();
            for (Element element : elements) {
                String name = element.text();
                String link = element.attr("href");
                //Провеверяем, есть ли данный кузов в БД
                boolean isExist = false;
                for (BodyBrand bodyBrand : bodiesFromBase) {
                    if (bodyBrand.getBodyName().equals(name) && bodyBrand.getLink().equals(link)) {
                        //Данный кузов уже есть в БД
                        bodies.add(bodyBrand);
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    //Такого кузова нет в БД, добавляем его туда
                    BodyBrand bodyBrand = new BodyBrand(name, link);
                    //Получение пропущенных комплектаций для данного кузова
                    bodyBrand.getEquipments().addAll(getEquipmentsSkipForBody(bodyBrand));
//                    bodyBrandRepository.save(bodyBrand);
                    bodies.add(bodyBrand);
                }
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getBodiesSkipForModel", carModel.getModelName()));
            e.printStackTrace();
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getBodiesSkipForModel", carModel.getModelName()));
        return bodies;
    }

    //Получение комплектаций для данного кузова
    private List<Equipment> getEquipmentsForBody(BodyBrand bodyBrand) {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getEquipmentsForBody", bodyBrand.getBodyName()));
        List<Equipment> equipments = new ArrayList<>();
        try {
            Document doc = getDocumentFromHtml(site + bodyBrand.getLink());
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
                //Получение групп для данной комплектации
                equipment.getGroups().addAll(getGroupsForEquipment(equipment));
                equipmentRepository.save(equipment);
                equipments.add(equipment);
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getEquipmentsForBody", bodyBrand.getBodyName()));
            e.printStackTrace();
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getEquipmentsForBody", bodyBrand.getBodyName()));
        return equipments;
    }

    //Получение пропущенных комплектаций для данного кузова
    private List<Equipment> getEquipmentsSkipForBody(BodyBrand bodyBrand) {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getEquipmentsSkipForBody", bodyBrand.getBodyName()));
        List<Equipment> equipments = new ArrayList<>();
        try {
            Document doc = getDocumentFromHtml(site + bodyBrand.getLink());
            Element table = doc.select(".table").first();
            Elements rows = table.select("tr");
            //Получаем список всех комплектаций из БД, возможно некоторые комплектации уже сохранены
            Iterable<Equipment> equipmentsFromBase = equipmentRepository.findAll();
            for (int i = 2; i < rows.size(); i++) {
                String name = rows.get(i).select("td").get(0).text();
                String link = rows.get(i).select("td").get(0).select("a").attr("href");
                //Проверяем есть ли данная комплектация в БД
                boolean isExist = false;
                for (Equipment equipment : equipmentsFromBase) {
                    if (equipment.getEquipmentName().equals(name) && equipment.getLink().equals(link)) {
                        //Данная комплектация уже есть в БД
                        equipments.add(equipment);
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    //Данной комплектации нет в БД - сохраняем ее
                    Equipment equipment = new Equipment();
                    equipment.setLink(link);
                    equipment.setEquipmentName(name);
                    equipment.setEngine(rows.get(i).select("td").get(1).text());
                    equipment.setPeriod(rows.get(i).select("td").get(2).text());
                    equipment.setBody(rows.get(i).select("td").get(3).text());
                    equipment.setGrade(rows.get(i).select("td").get(4).text());
                    equipment.setKpp(rows.get(i).select("td").get(5).text());
                    equipment.setAnother(rows.get(i).select("td").get(6).text());
                    //Получение пропущенных групп для данной комплектации
                    equipment.getGroups().addAll(getGroupsForEquipment(equipment));
                    equipmentRepository.save(equipment);
                    equipments.add(equipment);
                }
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getEquipmentsSkipForBody", bodyBrand.getBodyName()));
            e.printStackTrace();
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getEquipmentsSkipForBody", bodyBrand.getBodyName()));
        return equipments;
    }

    //Получение групп для данной комплектации
    private List<GroupComp> getGroupsForEquipment(Equipment equipment) {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getGroupsForEquipment", equipment.getEquipmentName()));
        List<GroupComp> groups = new ArrayList<>();
        //Iterable<GroupCatalog> groupsFromBase = groupCatalogRepository.findAll();
        try {
            Document doc = getDocumentFromHtml(site + equipment.getLink());
            Elements elements = doc.select("h3");
            for (int i = 0; i < elements.size() - 1; i++) {
                GroupComp group = new GroupComp();
                String groupName = elements.get(i).text();
                String link = elements.get(i).select("a").attr("href");
                group.setLink(link);
                //Проверяем, существует ли уже в каталоге групп такая группа
                boolean isExist = false;
                for (GroupCatalog groupCatalog : groupsFromBase) {
                    if (groupCatalog.getGroupName().equals(groupName)) {
                        //Существует
                        group.setGroupCatalog(groupCatalog);
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    //Не существует - создаем запись в каталоге групп
                    GroupCatalog groupCatalog = new GroupCatalog(groupName);
                    groupCatalogRepository.save(groupCatalog);
                    groupsFromBase.add(groupCatalog);
                    //Ссылку на вновь созданную запись сохраняем в текущей группе
                    group.setGroupCatalog(groupCatalog);
                }
                //Получение подгрупп для текущей группы
                group.getSubgroups().addAll(getSubgroupsForGroup(group));
//                groupRepository.save(group);
                groups.add(group);
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getGroupsForEquipment", equipment.getEquipmentName()));
            e.printStackTrace();
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getGroupsForEquipment", equipment.getEquipmentName()));
        return groups;
    }

    //Получение подгрупп для текущей группы
    private List<SubgroupComp> getSubgroupsForGroup(GroupComp group) {
        errorRepository.save(new ToyotaError("Begin", new Date(), "parse/getSubgroupsForGroup", group.getGroupCatalog().getGroupName()));
        List<SubgroupComp> subgroups = new ArrayList<>();
//        Iterable<SubgroupCatalog> subgroupsFromBase = subgroupCatalogRepository.findAll();
        try {
            Document doc = getDocumentFromHtml(site + group.getLink());
            Elements elements = doc.select(".parts_picture");
            for (Element element : elements) {
                SubgroupComp subgroup = new SubgroupComp();
                String subgroupName = element.attr("title");
                String link = element.parent().attr("href");
                String subgroupCode = element.parent().parent().text().substring(0, 5);
                subgroup.setLink(link);
                //Проверяем, существует ли уже в каталоге подгрупп такая подгруппа
                boolean isExist = false;
                for (SubgroupCatalog subgroupCatalog : subgroupsFromBase) {
                    if (subgroupCatalog.getSubgroupName().equals(subgroupName) && subgroupCatalog.getSubgroupCode().equals(subgroupCode)) {
                        //Существует
                        subgroup.setSubgroupCatalog(subgroupCatalog);
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    //Не существует - 1. Картинку скачиваем на диск
                    SubgroupCatalog subgroupCatalog = new SubgroupCatalog();
                    String src = element.attr("src");
                    String fileName = src.substring(src.lastIndexOf('/') + 1);
                    String randomString = UUID.randomUUID().toString();
                    subgroupCatalog.setPicture(randomString + " " + fileName);
                    URL url = new URL(site + src);
                    try {
                        BufferedImage image = ImageIO.read(url);
                        ImageIO.write(image, "png", new File(uploadPath + randomString + " " + fileName));
                    } catch (Exception ex) {
                        errorRepository.save(new ToyotaError("image = null", new Date(), "getSubgroupsForGroup", group.getGroupCatalog().getGroupName()));
                        subgroupCatalog.setPicture("nophoto.jpg");
                    }
                    // 2. Создаем запись в каталоге подгрупп
                    subgroupCatalog.setSubgroupName(subgroupName);
                    subgroupCatalog.setSubgroupCode(subgroupCode);
                    subgroupCatalogRepository.save(subgroupCatalog);
                    subgroupsFromBase.add(subgroupCatalog);
                    //Ссылку на вновь созданную запись сохраняем в текущей подгруппе
                    subgroup.setSubgroupCatalog(subgroupCatalog);
                }
                //Получение деталей для текущей подгруппы
                subgroup.getComponents().addAll(getComponentsForSubgroup(subgroup));
//                subgroupRepository.save(subgroup);
                subgroups.add(subgroup);
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getSubgroupsForGroup", group.getGroupCatalog().getGroupName()));
            e.printStackTrace();
        }
        errorRepository.save(new ToyotaError("End", new Date(), "parse/getSubgroupsForGroup", group.getGroupCatalog().getGroupName()));
        return subgroups;
    }

    //Получение деталей для текущей подгруппы
    private List<Component> getComponentsForSubgroup(SubgroupComp subgroup) {
        List<Component> components = new ArrayList<>();
        try {
            Document doc = getDocumentFromHtml(site + subgroup.getLink());
            Elements tables = doc.select("table");
            Elements redLables = doc.select(".red");
            if (redLables.size() > 0) {
                //Для данной группы нет деталей
                return components;
            }
            for (Element table : tables) {
                Elements elements = table.select(".detail-list a");
                if (elements.size() == 0) {
                    //Для данной таблицы нет деталей, идем дальше
                    continue;
                }
                //копируем картинку на диск
                String src = table.select("#part_image img").attr("src");
                String fileName = src.substring(src.lastIndexOf('/') + 1);
                String randomString = UUID.randomUUID().toString();
                URL url = new URL(site + src);
                String fileNameForBase = randomString + " " + fileName;
                try {
                    BufferedImage image = ImageIO.read(url);
                    ImageIO.write(image, "png", new File(uploadPath + randomString + " " + fileName));
                } catch (Exception ex) {
                    errorRepository.save(new ToyotaError(ex.getMessage(), new Date(), "getComponentsForSubgroup", subgroup.getSubgroupCatalog().getSubgroupName()));
                    fileNameForBase = "nophoto.jpg";
                }
                //получаем список деталей
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
//                    componentRepository.save(component);
                    components.add(component);
                }
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getComponentsForSubgroup", subgroup.getSubgroupCatalog().getSubgroupName()));
            e.printStackTrace();
        }
        return components;
    }

    private void getUnitListForComponent(Component component) {
        List<Unit> units = new ArrayList<>();
        try {
            Document doc = getDocumentFromHtml(site + component.getLink());
            Elements rows = doc.select(".parts-in-stock-widget_part-row");
            if (rows.size() == 0) {
                //Нет деталей
                return;
            }
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

    private Document getDocumentFromHtml(String page) {
        Document doc = null;
        try {
            while (true) {
                ProxyAddr currentProxy = proxyAddrList.get(proxyCount);
//                String command = "curl --max-time 5 -U " + currentProxy.getUser() + ":" + currentProxy.getPassword() + " -x " + currentProxy.getAddress() + ":" + currentProxy.getPort() + " " + page;
                String command = "curl -U " + currentProxy.getUser() + ":" + currentProxy.getPassword() + " -x " + currentProxy.getAddress() + ":" + currentProxy.getPort() + " " + page;
                ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
                Process process = processBuilder.start();
                doc = Jsoup.parse(process.getInputStream(), "utf-8", "");
                process.destroy();
                Elements path = doc.select(".path");
                if (path.size() != 0) {
                    break;
                }
                proxyCount++;
                //errorRepository.save(new ToyotaError(currentProxy.getAddress() + " - empty", new Date(), "getDocumentFromHtml", page));
                if (proxyCount >= proxyAddrList.size()) {
                    proxyCount = 0;
                }
            }
            count++;
            if (count >= REQUEST_COUNT) {
                count = 0;
                proxyCount++;
                if (proxyCount >= proxyAddrList.size()) {
                    proxyCount = 0;
                }
            }
        } catch (Exception e) {
            errorRepository.save(new ToyotaError(e.getMessage(), new Date(), "getDocumentFromHtml", page));
            e.printStackTrace();
        }
        return doc;
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
