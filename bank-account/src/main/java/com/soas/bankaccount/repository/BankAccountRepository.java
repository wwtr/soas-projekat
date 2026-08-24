package com.soas.bankaccount.repository;

import com.soas.bankaccount.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByEmail(String email);

    Optional<BankAccount> findByEmailAndCurrencyCode(String email, String currencyCode);

    void deleteByEmail(String email);
}
