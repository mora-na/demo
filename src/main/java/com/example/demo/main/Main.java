package com.example.demo.main;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Main {

    public static void main(String[] args) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("name", "张三 ");
        paramMap.put("age", "18");
        paramMap.put("address", "上海");

        log.info(paramMap.compute("name", Main::trim));
        log.info(paramMap.computeIfAbsent("name", Main::trim));
        log.info(paramMap.computeIfPresent("name", Main::trim));
    }

    private static String trim(String key, String value) {
        return trim(value);
    }

    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

}
