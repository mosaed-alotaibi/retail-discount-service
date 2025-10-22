package io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.document;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.util.Objects;


@Document(collection = "customers")
public class CustomerDocument {

    @Id
    private String customerId;

    @Field("customer_type")
    @Indexed
    private String customerType;

    @Field("registration_date")
    @Indexed
    private LocalDate registrationDate;

    @Field("password")
    private String password;

    public CustomerDocument() {
    }

    public CustomerDocument(String customerId, String customerType, LocalDate registrationDate, String password) {
        this.customerId = customerId;
        this.customerType = customerType;
        this.registrationDate = registrationDate;
        this.password = password;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerDocument that = (CustomerDocument) o;
        return Objects.equals(customerId, that.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

    @Override
    public String toString() {
        return String.format("CustomerDocument{id='%s', type='%s', registered=%s}",
                customerId, customerType, registrationDate);
    }
}
