package com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.OrdenListaItem;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.OrdenTrabajo;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, UUID> {

    @Query("""
        select new com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.OrdenListaItem(
        ot.idOrden,
        ot.nroOrden,
        c.idCliente,
        c.nombre,
        e.nombre,
        p.nombre,
        ot.descripcion,
        ot.creadaEn
        )
        from OrdenTrabajo ot
        join ot.cliente c
        join ot.estadoActual e
        join ot.prioridad p
        where (:idCliente is null or c.idCliente = :idCliente)
        and (:idTipoServicio is null or ot.tipoServicio.idTipoServicio = :idTipoServicio)
        and (:idPrioridad is null or p.idPrioridad = :idPrioridad)
        and (:idEstado is null or e.idEstado = :idEstado)
        and (
            coalesce(:q,'') = ''
            or lower(ot.nroOrden) like lower(concat('%',:q,'%'))
            or lower(ot.descripcion) like lower(concat('%',:q,'%'))
        )
        and (
            (:desde is null or :hasta is null) or
            ot.creadaEn between :desde and :hasta
        )
    """)
    Page<OrdenListaItem> buscarListado(
        @Param("idCliente") UUID idCliente,
        @Param("idTipoServicio") Integer idTipoServicio,
        @Param("idPrioridad") Integer idPrioridad,
        @Param("idEstado") Integer idEstado,
        @Param("desde") Instant desde,
        @Param("hasta") Instant hasta,
        @Param("q") String q,
        Pageable pageable
    );
}
