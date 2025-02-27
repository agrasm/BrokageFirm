package com.mehmet.brokagefirm.repository;

import com.mehmet.brokagefirm.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findCustomerByNameAndPassword(String name, String password);

    Customer findCustomerByName(String name);
}
