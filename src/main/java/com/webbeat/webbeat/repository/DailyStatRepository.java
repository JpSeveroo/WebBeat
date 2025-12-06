package com.webbeat.webbeat.repository;

import com.webbeat.webbeat.model.DailyStat;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDate;
import java.util.List;

public interface DailyStatRepository extends MongoRepository<DailyStat, String> {

    /**
     * Busca as estatísticas diárias de um usuário após uma determinada data, ordenadas por data.
     * @param ownerId A chave multi-tenant.
     * @param date A data mínima para o filtro.
     * @return Uma lista ordenada dos DailyStats.
     */
    List<DailyStat> findByOwnerIdAndDateAfterOrderByDateAsc(String ownerId, LocalDate date);
}