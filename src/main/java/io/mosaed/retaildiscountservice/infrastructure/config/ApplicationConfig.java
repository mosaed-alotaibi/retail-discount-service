package io.mosaed.retaildiscountservice.infrastructure.config;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.port.in.CalculateBillUseCase;
import io.mosaed.retaildiscountservice.application.port.out.BillRepository;
import io.mosaed.retaildiscountservice.application.port.out.CustomerRepository;
import io.mosaed.retaildiscountservice.application.service.BillService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public CalculateBillUseCase calculateBillUseCase(
            CustomerRepository customerRepository,
            BillRepository billRepository) {
        return new BillService(customerRepository, billRepository);
    }
}
