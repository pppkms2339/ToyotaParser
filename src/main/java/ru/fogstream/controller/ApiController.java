package ru.fogstream.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.fogstream.entity.CarModel;
import ru.fogstream.repository.ModelRepository;

@Controller
@RequestMapping("/api")
public class ApiController {

    @Autowired
    ModelRepository modelRepository;

    @GetMapping("/getModels")
    @ResponseBody
    public Iterable<CarModel> getAllCarModels() {
        return modelRepository.findAll();
    }

}
