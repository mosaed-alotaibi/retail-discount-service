package io.mosaed.retaildiscountservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;

@SpringBootTest(
    properties = {
        "spring.data.mongodb.auto-index-creation=false"
    },
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters({})
class RetailDiscountServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context can be loaded
        // MongoDB auto-index creation is disabled to avoid authentication issues
    }

}
