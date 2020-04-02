package ru.fogstream.controller;

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
import ru.fogstream.repository.BodyBrandRepository;
import ru.fogstream.repository.ModelRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DefaultController {

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    BodyBrandRepository bodyBrandRepository;

    @Value("${site.name}")
    private String site;

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
    public void parse(Model model) {
        modelRepository.deleteAll();
        long start = System.currentTimeMillis();
        //Модели
        try {
//            String command = "curl " + site;
//            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
//            Process process = processBuilder.start();
//            Document doc = Jsoup.parse(process.getInputStream(),"UTF-8", "");
            Document doc = Jsoup.connect(site).get();
            Elements elements = doc.select(".category2 li h4 a");
            for (Element element : elements) {
                CarModel carModel = new CarModel(element.text(), element.attr("href"));
                modelRepository.save(carModel);
            }
//            process.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        }
//        //Марки кузовов
//        try {
//            Iterable<CarModel> carModels = modelRepository.findAll();
//            for (CarModel carModel : carModels) {
//                String command = "curl " + site + carModel.getLink();
//                ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
//                Process process = processBuilder.start();
//                Document doc2 = Jsoup.parse(process.getInputStream(),"UTF-8", "");
//                Elements elements = doc2.select(".category2 li h4 a");
//                for (Element element : elements) {
//                    BodyBrand bodyBrand = new BodyBrand(element.text(), element.attr("href"));
//                    carModel.getBodyBrands().add(bodyBrand);
//                    bodyBrandRepository.save(bodyBrand);
//                }
//                process.destroy();
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.out.println("Время работы: " + (System.currentTimeMillis() - start) / 1000);
    }

    @GetMapping("/{carModelId}")
    public String bodyBrand(@PathVariable("carModelId") Long id, Model model) {
        CarModel car = modelRepository.findById(id).get();
        List<BodyBrand> bodyBrands = car.getBodyBrands();
        model.addAttribute("car", car.getModelName());
        model.addAttribute("bodyBrands", bodyBrands);
        return "bodyBrand";
    }

    @GetMapping("/searchBodyNumber")
    public String searchBodyNumber(@RequestParam("param") String param, Model model) {
        try {
            Document doc = Jsoup.connect(site + "/search_frame/?frame_no=" + param).get();
            Elements elements = doc.select(".red");
            if(elements.size() > 0) {
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
}
