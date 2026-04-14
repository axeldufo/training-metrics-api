package com.axel.trainingmetricsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication
public class TrainingMetricsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingMetricsApiApplication.class, args);
    }

}
