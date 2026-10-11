package com.lockedin.backend.repository;

import com.lockedin.backend.entity.CreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {
    List<CreditTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("select coalesce(sum(c.amount), 0) from CreditTransaction c where c.user.id = :userId")
    int getBalanceForUser(@Param("userId") Long userId);
}
