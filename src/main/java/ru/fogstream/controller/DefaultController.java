package ru.fogstream.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.fogstream.config.DataSourceConfig;
import ru.fogstream.entity.CarModel;
import ru.fogstream.entity.ProxyAddr;
import ru.fogstream.entity.ToyotaError;
import ru.fogstream.repository.*;
import ru.fogstream.utility.Mail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

//    @Scheduled(cron = "0 0 6-23 * * *", zone = "Asia/Vladivostok")
//    public void report() {
//        SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
//        List<ToyotaError> errors = errorRepository.getLastErrors();
//        StringBuffer sb = new StringBuffer();
//        for (ToyotaError error : errors) {
//            sb.append(error.getId()).append("\t")
//                    .append(error.getAdditionalInfo()).append("\t")
//                    .append(f.format(error.getDate())).append("\t")
//                    .append(error.getMessage()).append("\t")
//                    .append(error.getMethod()).append(System.lineSeparator());
//        }
//        try {
//            new Mail().send("Отчет за " + f.format(new Date()), sb.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
