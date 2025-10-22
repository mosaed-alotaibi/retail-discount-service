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

/**
 * Configuration class for application layer beans.
 *
 * WHY do we need this configuration class?
 *
 * Our application layer (BillService) is pure Java with no Spring annotations.
 * This is intentional - we want the application layer to be framework-agnostic.
 * But Spring needs to know about these classes to inject them into controllers.
 *
 * This configuration class bridges the gap. It tells Spring how to create
 * application layer beans and wire them with dependencies.
 *
 * ALTERNATIVE APPROACH:
 *
 * We could annotate BillService with @Service and let Spring auto-discover it
 * through component scanning. That would be simpler and is perfectly acceptable.
 *
 * However, using explicit @Bean configuration demonstrates a more purist
 * approach to hexagonal architecture. The application layer has ZERO knowledge
 * of Spring. All Spring-specific code stays in the infrastructure layer
 * (this configuration class).
 *
 * Benefits of this approach:
 * - Application layer is truly framework-agnostic
 * - Clear separation between business logic and infrastructure
 * - Easy to test application layer without Spring
 * - Could swap Spring for a different framework by changing only config
 *
 * Drawback:
 * - Slightly more verbose (need explicit configuration)
 *
 * For this assessment, I'll show both approaches. You can use whichever you
 * prefer, as both are valid architectural choices.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Create the BillService bean.
     *
     * @Bean tells Spring to call this method and register the return value
     * as a Spring-managed bean. Spring will invoke this method once at startup,
     * cache the result, and inject it wherever CalculateBillUseCase is needed.
     *
     * The method parameters (CustomerRepository, BillRepository) are automatically
     * resolved by Spring. It looks for beans matching those types and injects
     * them. In our case, it finds MongoCustomerRepository and MongoBillRepository.
     *
     * DEPENDENCY FLOW:
     *
     * BillController needs CalculateBillUseCase
     *   → Spring finds this @Bean method returns CalculateBillUseCase
     *   → Spring calls calculateBillUseCase(customerRepo, billRepo)
     *   → Spring injects MongoCustomerRepository and MongoBillRepository
     *   → Spring creates BillService with those dependencies
     *   → Spring injects BillService into BillController
     *
     * All of this is automatic! Spring figures out the dependency graph and
     * wires everything together at startup. If there are circular dependencies
     * or missing beans, you get an error at startup, not at runtime.
     */
    @Bean
    public CalculateBillUseCase calculateBillUseCase(
            CustomerRepository customerRepository,
            BillRepository billRepository) {
        return new BillService(customerRepository, billRepository);
    }

    /**
     * ALTERNATIVE: If you prefer a simpler approach, you can skip this
     * configuration class entirely and just annotate BillService with @Service:
     *
     * @Service
     * public class BillService implements CalculateBillUseCase {
     *     ...
     * }
     *
     * Spring will auto-discover it through component scanning and wire it up
     * automatically. This is less code and works perfectly fine.
     *
     * The explicit @Bean approach shown above is more "pure" from a hexagonal
     * architecture perspective, but the @Service annotation is more pragmatic
     * and commonly used in real projects.
     *
     * Both approaches are correct. Choose based on your preference:
     * - @Bean configuration = more explicit, framework-agnostic
     * - @Service annotation = simpler, more conventional Spring Boot style
     */
}
