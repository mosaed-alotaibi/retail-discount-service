package io.mosaed.retaildiscountservice.infrastructure.config;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * Access Swagger UI at: http://localhost:8080/api/v1/swagger-ui.html
 * Access OpenAPI spec at: http://localhost:8080/api/v1/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${application.version:1.0.0}")
    private String version;

    @Bean
    public OpenAPI retailDiscountServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Retail Discount Service API")
                        .description("REST API for calculating retail discounts based on customer type. " +
                                "This service implements a two-tier discount system: " +
                                "percentage-based discounts (Employee: 30%, Affiliate: 10%, Long-term customer: 5%) " +
                                "and bill-based discounts ($5 for every $100).")
                        .version(version)
                        .contact(new Contact()
                                .name("Mosaed Alotaibi")
                                .email("mosaed@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api/v1")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.example.com/v1")
                                .description("Production server")))
                .addSecurityItem(new SecurityRequirement().addList("HTTP Basic Auth"))
                .schemaRequirement("HTTP Basic Auth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")
                        .description("HTTP Basic Authentication with customer credentials"));
    }
}
