package com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, UUID>{

}
