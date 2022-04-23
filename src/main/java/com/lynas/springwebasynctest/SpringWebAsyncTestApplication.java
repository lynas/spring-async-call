package com.lynas.springwebasynctest;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableAsync
public class SpringWebAsyncTestApplication implements AsyncConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(SpringWebAsyncTestApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
}


@RestController
class DemoRestController {

    private final DemoService demoService;

    DemoRestController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/callAsync")
    public List<String> dem() throws Exception {
        var start = System.currentTimeMillis();
        CompletableFuture<List<String>> r1 = demoService.m1();
        CompletableFuture<List<String>> r2 = demoService.m2();
        List<String> result = new ArrayList<>();
        result.addAll(r1.get());
        result.addAll(r2.get());
        var end = System.currentTimeMillis();
        System.out.println(end-start);
        return result;
    }

}


@Service
class DemoService {
    private final RestTemplate restTemplate;


    DemoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Async
    public CompletableFuture<List<String>> m1() throws Exception{
        try {
            var result = restTemplate.getForObject("http://localhost:8081/demo1", String[].class);
            return CompletableFuture.completedFuture(Arrays.asList(result));
        } catch (Exception e) {
            throw e;
        }
    }

    @Async
    public CompletableFuture<List<String>> m2() throws Exception{
        var result = restTemplate.getForObject("http://localhost:8081/demo1", String[].class);
        return CompletableFuture.completedFuture(Arrays.asList(result));
    }
}

//TODO not done properly yet
class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        System.out.println("Exception message - " + ex.getMessage());
        System.out.println("Method name - " + method.getName());
        for (Object param : params) {
            System.out.println("Param - " + param);
        }
    }
}

















