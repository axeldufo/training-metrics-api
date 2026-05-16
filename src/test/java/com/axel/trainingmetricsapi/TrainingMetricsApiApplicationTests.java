package com.axel.trainingmetricsapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestContainersConfiguration.class)
class TrainingMetricsApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
