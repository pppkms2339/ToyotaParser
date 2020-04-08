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
import ru.fogstream.entity.BodyBrand;
import ru.fogstream.entity.CarModel;
import ru.fogstream.entity.Equipment;
import ru.fogstream.entity.ProxyAddr;
import ru.fogstream.repository.BodyBrandRepository;
import ru.fogstream.repository.EquipmentRepository;
import ru.fogstream.repository.ModelRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class DefaultController {

    private static int REQUEST_COUNT = 25;
    private int count = 0, proxyCount = 0;
    private List<ProxyAddr> proxyAddrList = null;

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    BodyBrandRepository bodyBrandRepository;

    @Autowired
    EquipmentRepository equipmentRepository;

    @Value("${site.name}")
    private String site;

    @Value("${proxy.url}")
    private String url;

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
        //Получение всех моделей
        getCarModelForToyota();
        //Получение кузовов для каждой модели
        Iterable<CarModel> carModels = modelRepository.findAll();
        for(CarModel carModel : carModels) {
            List<BodyBrand> bodyBrands = getBodyBrandListForCar(carModel);
            carModel.getBodyBrands().addAll(bodyBrands);
            modelRepository.save(carModel);
        }
        //Получение комплектаций для каждого кузова
        Iterable<BodyBrand> bodyBrands = bodyBrandRepository.findAll();
        for(BodyBrand bodyBrand : bodyBrands) {
            List<Equipment> equipments = getEquipmentListForBody(bodyBrand);
            bodyBrand.getEquipments().addAll(equipments);
            bodyBrandRepository.save(bodyBrand);
        }
        System.out.println("Время работы: " + (System.currentTimeMillis() - start) / 1000);
    }

    //Модели
    private void getCarModelForToyota() {
        try {
            String pageHtml = getPageHtml(site);
            Document doc = Jsoup.parse(pageHtml);
            Elements elements = doc.select(".category2 li h4 a");
            for (Element element : elements) {
                CarModel carModel = new CarModel(element.text(), element.attr("href"));
                modelRepository.save(carModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return bodyBrands;
    }

    //Комплектации
    private List<Equipment> getEquipmentListForBody(BodyBrand bodyBrand) throws IOException {
        List<Equipment> equipments = new ArrayList<>();
        try {
            String pageHtml = getPageHtml(site + bodyBrand.getLink());
            Document doc = Jsoup.parse(pageHtml);
            Element table = doc.select(".table").first();
            Elements rows = table.select("tr");
            for (int i = 2; i < rows.size(); i++) {
                Equipment equipment = new Equipment();
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
            e.printStackTrace();
        }
        return equipments;
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
            String command = "Curl -U " + currentProxy.getUser() + ":" + currentProxy.getPassword() + " -x " + currentProxy.getAddress() + ":" + currentProxy.getPort() + " " + page;
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            Process process = processBuilder.start();
            String line = null;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
