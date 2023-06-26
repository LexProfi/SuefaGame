package com.example.waruniverseserver.repositories;

import com.example.waruniverseserver.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByLogin(@Param("login") String login);
    Account findByLoginAndPassword(@Param("login") String login, @Param("password") String password);
}
