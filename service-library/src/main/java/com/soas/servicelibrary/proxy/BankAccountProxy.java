package com.soas.servicelibrary.proxy;

import com.soas.servicelibrary.dto.BankAccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "bank-account")
public interface BankAccountProxy {

    @GetMapping("/bank-account/user/{email}")
    List<BankAccountDto> getAccountsForUser(@PathVariable("email") String email);

    @GetMapping("/bank-account/user/{email}/{currencyCode}")
    BankAccountDto getAccount(@PathVariable("email") String email,
                              @PathVariable("currencyCode") String currencyCode);

    @GetMapping("/bank-account/internal/{email}")
    List<BankAccountDto> getAccountsInternal(@PathVariable("email") String email);

    @PostMapping("/bank-account/internal/{email}")
    BankAccountDto createStartingAccount(@PathVariable("email") String email);

    @DeleteMapping("/bank-account/internal/{email}")
    void deleteAccountsForUser(@PathVariable("email") String email);

    @PutMapping("/bank-account/internal/{email}/{currencyCode}")
    BankAccountDto changeAmount(@PathVariable("email") String email,
                                @PathVariable("currencyCode") String currencyCode,
                                @RequestParam("delta") BigDecimal delta);
}
