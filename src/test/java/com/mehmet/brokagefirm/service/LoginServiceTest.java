package com.mehmet.brokagefirm.service;

import com.mehmet.brokagefirm.dto.LoginDTO;
import com.mehmet.brokagefirm.entity.Customer;
import com.mehmet.brokagefirm.handler.BrokageLogicException;
import com.mehmet.brokagefirm.repository.CustomerRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class LoginServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @InjectMocks
    private LoginService loginService;

    private LoginDTO loginDTO;

    private Customer customer;

    private User user;

    private void initialize() {
        loginDTO = new LoginDTO();
        loginDTO.setName("admin");
        loginDTO.setPassword("1234");

        customer = new Customer();
        customer.setName("admin");
        customer.setPassword("1234");
        customer.setRole("ADMIN");

        user = (User) User.withDefaultPasswordEncoder()
                .username("admin")
                .password("1234")
                .roles("ADMIN")
                .build();
    }

    @Test
    void testCheckCredentials() {
        initialize();
        Mockito.doNothing().when(inMemoryUserDetailsManager)
                .createUser(any());
        when(customerRepository.findCustomerByNameAndPassword(any(), any())).thenReturn(customer);
        String auth = loginService.checkCredentials(loginDTO);
        Assertions.assertNotNull(auth);
        Assertions.assertEquals(LoginService.AUTHENTICATE, auth);
    }

    @Test
    void testCheckCredentialsThrowsExceptionWhenLoginFails() {
        initialize();
        when(customerRepository.findCustomerByNameAndPassword(any(), any())).thenReturn(null);
        Exception exception = Assertions.assertThrows(BrokageLogicException.class, () -> loginService.checkCredentials(loginDTO));

        Assertions.assertNotNull(exception.getMessage().equals("Invalid auth parameters"));

    }
}