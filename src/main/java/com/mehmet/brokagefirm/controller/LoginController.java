package com.mehmet.brokagefirm.controller;

import com.mehmet.brokagefirm.dto.LoginDTO;
import com.mehmet.brokagefirm.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @PostMapping
    public String login(@RequestBody LoginDTO login) {
        return loginService.checkCredentials(login);
    }
}