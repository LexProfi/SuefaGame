package com.example.waruniverseserver.repositories;

import com.example.waruniverseserver.domain.Account;
import com.example.waruniverseserver.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Game findFirstByAccountAndResultIsNull(@Param("account") Account account);
}
