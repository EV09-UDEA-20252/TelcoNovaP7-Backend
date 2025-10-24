package com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.OrdenListaItem;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.OrdenTrabajo;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, UUID> {
    @Query("select ot from OrdenTrabajo ot where ot.idOrden = :id and ot.eliminada = false")
    Optional<OrdenTrabajo> findByIdAndEliminadaFalse(@Param("id") UUID id);

    @Query("select coalesce(max(ot.consecutivo), -1) from OrdenTrabajo ot")
    Long findMaxConsecutivo();

    boolean existsByNroOrden(String nroOrden);

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
            where ot.eliminada = false
            and (:idCliente is null or c.idCliente = :idCliente)
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
            Pageable pageable);

    @Query("""
            select e.nombre as estado, count(ot) as total
            from OrdenTrabajo ot
            join ot.estadoActual e
            where ot.eliminada = false
                and (:idCliente is null or ot.cliente.idCliente = :idCliente)
                and (:idTipoServicio is null or ot.tipoServicio.idTipoServicio = :idTipoServicio)
                and (cast(:desde as instant) is null or ot.creadaEn >= :desde)
                and (cast(:hasta as instant) is null or ot.creadaEn <= :hasta)
            group by e.nombre
            """)
    List<Object[]> contarPorEstado(
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            @Param("idCliente") UUID idCliente,
            @Param("idTipoServicio") Integer idTipoServicio);

    @Query("""
            select e.nombre, count(ot)
            from OrdenTrabajo ot
            join ot.estadoActual e
            where ot.eliminada = false
                and ot.creadaEn between :desde and :hasta
                and (:idCliente is null or ot.cliente.idCliente = :idCliente)
                and (:idTipoServicio is null or ot.tipoServicio.idTipoServicio = :idTipoServicio)
            group by e.nombre
            """)
    List<Object[]> conteoPorEstado(@Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            @Param("idCliente") UUID idCliente,
            @Param("idTipoServicio") Integer idTipoServicio);

    @Query("""
            select p.nombre, count(ot)
            from OrdenTrabajo ot
            join ot.prioridad p
            where ot.eliminada = false
                and ot.creadaEn between :desde and :hasta
                and (:idCliente is null or ot.cliente.idCliente = :idCliente)
                and (:idTipoServicio is null or ot.tipoServicio.idTipoServicio = :idTipoServicio)
            group by p.nombre
            """)
    List<Object[]> conteoPorPrioridad(@Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            @Param("idCliente") UUID idCliente,
            @Param("idTipoServicio") Integer idTipoServicio);

    @Query("""
            select ts.nombre, count(ot)
            from OrdenTrabajo ot
            join ot.tipoServicio ts
            where ot.eliminada = false
                and ot.creadaEn between :desde and :hasta
                and (:idCliente is null or ot.cliente.idCliente = :idCliente)
                and (:idTipoServicio is null or ot.tipoServicio.idTipoServicio = :idTipoServicio)
            group by ts.nombre
            """)
    List<Object[]> conteoPorTipoServicio(@Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            @Param("idCliente") UUID idCliente,
            @Param("idTipoServicio") Integer idTipoServicio);

    @Query("""
            select cast(ot.creadaEn as date), count(ot)
            from OrdenTrabajo ot
            where ot.eliminada = false
                and ot.creadaEn between :desde and :hasta
                and (:idCliente is null or ot.cliente.idCliente = :idCliente)
                and (:idTipoServicio is null or ot.tipoServicio.idTipoServicio = :idTipoServicio)
            group by cast(ot.creadaEn as date)
            order by cast(ot.creadaEn as date)
            """)
    List<Object[]> conteoPorDia(@Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            @Param("idCliente") UUID idCliente,
            @Param("idTipoServicio") Integer idTipoServicio);
}