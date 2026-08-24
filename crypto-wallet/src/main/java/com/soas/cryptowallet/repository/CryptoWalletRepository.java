package com.soas.cryptowallet.repository;

import com.soas.cryptowallet.entity.CryptoWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CryptoWalletRepository extends JpaRepository<CryptoWallet, Long> {

    List<CryptoWallet> findByEmail(String email);

    Optional<CryptoWallet> findByEmailAndCryptoCode(String email, String cryptoCode);

    void deleteByEmail(String email);
}
