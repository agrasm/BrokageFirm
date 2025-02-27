package com.mehmet.brokagefirm.service;

import com.mehmet.brokagefirm.dto.LoginDTO;
import com.mehmet.brokagefirm.entity.Customer;
import com.mehmet.brokagefirm.handler.BrokageLogicException;
import com.mehmet.brokagefirm.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class LoginService {
    private final CustomerRepository customerRepository;
    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;
    public static final String AUTHENTICATE = "Authentication is completed";
    public static final String ADMIN = "ROLE_ADMIN";

    public String checkCredentials(LoginDTO login) {
        Customer customer = customerRepository.findCustomerByNameAndPassword(login.getName(), login.getPassword());
        if (!ObjectUtils.isEmpty(customer)) {
            UserDetails user = User.withDefaultPasswordEncoder()
                    .username(customer.getName())
                    .password(customer.getPassword())
                    .roles(customer.getRole())
                    .build();
            inMemoryUserDetailsManager.createUser(user);
            return AUTHENTICATE;
        } else {
            log.error("Attempted to login with invalid credentials: ", login.getName());
            throw new BrokageLogicException("Invalid auth parameters");
        }
    }

    public User getCurrentUser() {
        return ((User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal());
    }

    public String getCurrentUserRole() {
        List<GrantedAuthority> currentUserAuths = getCurrentUser().getAuthorities().stream().collect(Collectors.toList());
        return currentUserAuths.get(0).toString();
    }
}