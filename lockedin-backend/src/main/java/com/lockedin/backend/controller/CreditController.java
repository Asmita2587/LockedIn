package com.lockedin.backend.controller;

import com.lockedin.backend.dto.CreditBalanceResponse;
import com.lockedin.backend.service.CreditService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credits")
public class CreditController {

    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @GetMapping("/balance")
    public CreditBalanceResponse balance(@AuthenticationPrincipal Long userId) {
        return creditService.getBalanceResponse(userId);
    }
}
