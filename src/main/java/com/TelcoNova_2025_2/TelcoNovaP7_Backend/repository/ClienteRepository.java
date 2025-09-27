package com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
}
