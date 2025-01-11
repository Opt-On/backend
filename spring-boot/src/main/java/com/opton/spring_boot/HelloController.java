package com.opton.spring_boot;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    
    @GetMapping("/")
    public Map<String, String> index(){
        // Example json return 
        HashMap<String, String> map = new HashMap<>();
        map.put("deez", "nuts");
        return map;
    }
}
