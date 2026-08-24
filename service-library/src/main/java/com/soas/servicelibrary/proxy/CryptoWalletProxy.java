package com.soas.servicelibrary.proxy;

import com.soas.servicelibrary.dto.CryptoWalletDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "crypto-wallet")
public interface CryptoWalletProxy {

    @GetMapping("/crypto-wallet/user/{email}")
    List<CryptoWalletDto> getWalletsForUser(@PathVariable("email") String email);

    @GetMapping("/crypto-wallet/user/{email}/{cryptoCode}")
    CryptoWalletDto getWallet(@PathVariable("email") String email,
                              @PathVariable("cryptoCode") String cryptoCode);

    @GetMapping("/crypto-wallet/internal/{email}")
    List<CryptoWalletDto> getWalletsInternal(@PathVariable("email") String email);

    @PostMapping("/crypto-wallet/internal/{email}")
    CryptoWalletDto createStartingWallet(@PathVariable("email") String email);

    @DeleteMapping("/crypto-wallet/internal/{email}")
    void deleteWalletsForUser(@PathVariable("email") String email);

    @PutMapping("/crypto-wallet/internal/{email}/{cryptoCode}")
    CryptoWalletDto changeAmount(@PathVariable("email") String email,
                                 @PathVariable("cryptoCode") String cryptoCode,
                                 @RequestParam("delta") BigDecimal delta);
}
