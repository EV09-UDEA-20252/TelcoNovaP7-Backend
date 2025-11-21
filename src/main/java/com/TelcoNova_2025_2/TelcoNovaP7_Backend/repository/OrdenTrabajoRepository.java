package com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.OrdenListaItem;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.OrdenTrabajo;

import jakarta.persistence.Tuple;

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
        
    @Query(
    value = """
        select * from consultar_ordenes(
            cast(:idCliente as uuid), 
            cast(:idTipoServicio as int), 
            cast(:idPrioridad as int), 
            cast(:idEstado as int),
            :q, 
            cast(:desde as timestamp), 
            cast(:hasta as timestamp)
        )
        """,
    countQuery = """
        select count(*) from consultar_ordenes(
            cast(:idCliente as uuid), 
            cast(:idTipoServicio as int), 
            cast(:idPrioridad as int), 
            cast(:idEstado as int),
            :q, 
            cast(:desde as timestamp), 
            cast(:hasta as timestamp)
        )
        """,
    nativeQuery = true
        )
        Page<Tuple> buscarListado(
                @Param("idCliente") UUID idCliente,
                @Param("idTipoServicio") Integer idTipoServicio,
                @Param("idPrioridad") Integer idPrioridad,
                @Param("idEstado") Integer idEstado,
                @Param("desde") Instant desde,
                @Param("hasta") Instant hasta,
                @Param("q") String q,
                Pageable pageable
        );
    @Query(value = """
    select e.nombre, count(ot.id_orden)
    from orden_trabajo ot
    join estado_orden e on e.id_estado = ot.id_estado_ac
    where ot.eliminada = false
      and ot.creado_en between :desde and :hasta
      and (:idCliente is null or ot.id_cliente = :idCliente)
      and (:idTipoServicio is null or ot.id_tipo_servic = :idTipoServicio)
    group by e.nombre
    """,
    nativeQuery = true)
List<Object[]> contarPorEstado(
        @Param("desde") Instant desde,
        @Param("hasta") Instant hasta,
        @Param("idCliente") UUID idCliente,
        @Param("idTipoServicio") Integer idTipoServicio
);

        @Query(value = """
    select p.nombre, count(ot.id_orden)
    from orden_trabajo ot
    join prioridad p on p.id_prioridad = ot.id_prioridad
    where ot.eliminada = false
      and ot.creado_en between :desde and :hasta
      and (:idCliente is null or ot.id_cliente = :idCliente)
      and (:idTipoServicio is null or ot.id_tipo_servic = :idTipoServicio)
    group by p.nombre
    """,
    nativeQuery = true)
List<Object[]> contarPorPrioridad(
        @Param("desde") Instant desde,
        @Param("hasta") Instant hasta,
        @Param("idCliente") UUID idCliente,
        @Param("idTipoServicio") Integer idTipoServicio
);

   @Query(value = """
    select ts.nombre, count(ot.id_orden)
    from orden_trabajo ot
    join tipo_servicio ts on ts.id_tipo_servic = ot.id_tipo_servic
    where ot.eliminada = false
      and ot.creado_en between :desde and :hasta
      and (:idCliente is null or ot.id_cliente = :idCliente)
      and (:idTipoServicio is null or ot.id_tipo_servic = :idTipoServicio)
    group by ts.nombre
    """,
    nativeQuery = true)
List<Object[]> contarPorTipoServicio(
        @Param("desde") Instant desde,
        @Param("hasta") Instant hasta,
        @Param("idCliente") UUID idCliente,
        @Param("idTipoServicio") Integer idTipoServicio
);
   @Query(value = """
    select cast(ot.creado_en as date) as fecha, count(ot.id_orden)
    from orden_trabajo ot
    where ot.eliminada = false
      and ot.creado_en between :desde and :hasta
      and (:idCliente is null or ot.id_cliente = :idCliente)
      and (:idTipoServicio is null or ot.id_tipo_servic = :idTipoServicio)
    group by cast(ot.creado_en as date)
    order by cast(ot.creado_en as date)
    """,
    nativeQuery = true)
List<Object[]> contarPorDia(
        @Param("desde") Instant desde,
        @Param("hasta") Instant hasta,
        @Param("idCliente") UUID idCliente,
        @Param("idTipoServicio") Integer idTipoServicio
);

}