package com.TelcoNova_2025_2.TelcoNovaP7_Backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import com.TelcoNova_2025_2.TelcoNovaP7_Backend.common.ApiException;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.informe.InformeOrdenesResp;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.CrearOrdenRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.EditarOrdenRequest;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.FiltroOrdenes;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.OrdenCreadaResponse;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.dto.orden.OrdenListaItem;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Cliente;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.EstadoOrden;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.HistorialEstado;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Notificacion;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.OrdenTrabajo;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Prioridad;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Rol;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.TipoServicio;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.model.Usuario;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.ClienteRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.EstadoOrdenRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.HistorialEstadoRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.NotificacionRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.OrdenTrabajoRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.PrioridadRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.TipoServicioRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.repository.UsuarioRepository;
import com.TelcoNova_2025_2.TelcoNovaP7_Backend.service.impl.OrdenServiceImpl;

import jakarta.persistence.EntityManager;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class OrdenServiceTest {
    @Mock
    private OrdenTrabajoRepository ordenRepo;
    @Mock
    private ClienteRepository clienteRepo;
    @Mock
    private PrioridadRepository prioridadRepo;
    @Mock
    private TipoServicioRepository tipoServicioRepo;
    @Mock
    private EstadoOrdenRepository estadoRepo;
    @Mock
    private HistorialEstadoRepository historialRepo;
    @Mock
    private NotificacionRepository notifRepo;
    @Mock
    private UsuarioRepository usuarioRepo;
    @Mock
    private EntityManager em;
    @Mock
    private Clock clock;

    @InjectMocks
    private OrdenServiceImpl serviceImpl;

    private UUID invokeCurrentUserIdOrNull() throws Exception {
        Method m = OrdenServiceImpl.class.getDeclaredMethod("currentUserIdOrNull");
        m.setAccessible(true);
        return (UUID) m.invoke(serviceImpl);
    }

    @Test
    void currentUserIdOrNullWithNullAuth() throws Exception {
        //Arrange
        SecurityContextHolder.clearContext();

        //Act
        UUID result = invokeCurrentUserIdOrNull();

        //Assert
        assertNull(result);
    }

    void currentUserIdOrNullWithValidString() throws Exception {
        //Arrange
        UUID id = UUID.randomUUID();
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(id.toString());

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        //Act
        UUID result = invokeCurrentUserIdOrNull();

        //Assert
        assertEquals(id, result);
    }

    @Test
    void currentUserIdOrNullWithInvalidString() throws Exception {
        //Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("no-es-uuid");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        //Act
        UUID result = invokeCurrentUserIdOrNull();

        //Assert
        assertNull(result);
    }

    @Test
    void createOrderSuccesful() {
        //Arrange
        UUID idCliente = UUID.randomUUID();
        UUID idCreador = UUID.randomUUID();

        CrearOrdenRequest req = new CrearOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden 1", 
            Instant.parse("2025-01-02T12:00:00Z"));

        var cliente = new Cliente();
        cliente.setIdCliente(idCliente);
        cliente.setNombre("Cliente 1");

        var prioridad =  new Prioridad();
        prioridad.setIdPrioridad(1);
        prioridad.setNombre("Alta");

        var tipo = new TipoServicio();
        tipo.setIdTipoServicio(1);
        tipo.setNombre("Reparacion");

        var estado = new EstadoOrden();
        estado.setIdEstado(1);
        estado.setNombre("ACTIVA");

        var supervisor = new Usuario();
        supervisor.setIdUsuario(UUID.randomUUID());
        supervisor.setRol(Rol.SUPERVISOR);

        when(clienteRepo.findById(idCliente)).thenReturn(Optional.of(cliente));
        when(prioridadRepo.findById(1)).thenReturn(Optional.of(prioridad));
        when(tipoServicioRepo.findById(1)).thenReturn(Optional.of(tipo));
        when(estadoRepo.findByNombre("ACTIVA")).thenReturn(Optional.of(estado));
        when(ordenRepo.findMaxConsecutivo()).thenReturn(5L);
        when(usuarioRepo.findAllByRol(Rol.SUPERVISOR)).thenReturn(List.of(supervisor));

        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);

        when(auth.getPrincipal()).thenReturn(idCreador);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);

        //Act
        OrdenCreadaResponse res = serviceImpl.crear(req);

        //Assert
        assertNotNull(res);
        assertEquals("00006", res.nroOrden());

        verify(ordenRepo).save(any(OrdenTrabajo.class));
        verify(historialRepo).save(any(HistorialEstado.class));
        verify(notifRepo, times(2)).save(any(Notificacion.class));
    }

    @Test
    void createOrderWithNotIdCreator() {
        //Arrange
        UUID idCliente = UUID.randomUUID();

        CrearOrdenRequest req = new CrearOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden 1", 
            Instant.parse("2025-01-02T12:00:00Z"));

        var cliente = new Cliente();
        cliente.setIdCliente(idCliente);
        cliente.setNombre("Cliente 1");

        var prioridad =  new Prioridad();
        prioridad.setIdPrioridad(1);
        prioridad.setNombre("Alta");

        var tipo = new TipoServicio();
        tipo.setIdTipoServicio(1);
        tipo.setNombre("Reparacion");

        var estado = new EstadoOrden();
        estado.setIdEstado(1);
        estado.setNombre("ACTIVA");

        var supervisor = new Usuario();
        supervisor.setIdUsuario(UUID.randomUUID());
        supervisor.setRol(Rol.SUPERVISOR);

        when(clienteRepo.findById(idCliente)).thenReturn(Optional.of(cliente));
        when(prioridadRepo.findById(1)).thenReturn(Optional.of(prioridad));
        when(tipoServicioRepo.findById(1)).thenReturn(Optional.of(tipo));
        when(estadoRepo.findByNombre("ACTIVA")).thenReturn(Optional.of(estado));
        when(ordenRepo.findMaxConsecutivo()).thenReturn(5L);
        when(usuarioRepo.findAllByRol(Rol.SUPERVISOR)).thenReturn(List.of(supervisor));

        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);

        when(auth.getPrincipal()).thenReturn(null);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);

        //Act
        OrdenCreadaResponse res = serviceImpl.crear(req);

        //Assert
        assertNotNull(res);
        assertEquals("00006", res.nroOrden());

        verify(ordenRepo).save(any(OrdenTrabajo.class));
        verify(historialRepo).save(any(HistorialEstado.class));
        verify(notifRepo).save(any(Notificacion.class));
    }

    @Test
    void createOrderWithNotExistingClient() {
        //Arrange
        UUID idCliente = UUID.randomUUID();

        CrearOrdenRequest req = new CrearOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden", 
            Instant.now());

        when(clienteRepo.findById(idCliente)).thenReturn(Optional.empty());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.crear(req));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Cliente no existe", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
        verify(notifRepo, never()).save(any());
    }

    @Test
    void createOrderWithNotExistingPriority() {
        //Arrange
        UUID idCliente = UUID.randomUUID();

        CrearOrdenRequest req = new CrearOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden", 
            Instant.now());

        when(clienteRepo.findById(idCliente)).thenReturn(Optional.of(new Cliente()));
        when(prioridadRepo.findById(1)).thenReturn(Optional.empty());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.crear(req));

        //Asert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Prioridad no existe", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
        verify(notifRepo, never()).save(any());
    }

    @Test
    void createOrderWithNotExistingServiceType() {
        //Arrange
        UUID idCliente = UUID.randomUUID();

        CrearOrdenRequest req = new CrearOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden", 
            Instant.now());

        when(clienteRepo.findById(idCliente)).thenReturn(Optional.of(new Cliente()));
        when(prioridadRepo.findById(1)).thenReturn(Optional.of(new Prioridad()));
        when(tipoServicioRepo.findById(1)).thenReturn(Optional.empty());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.crear(req));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Tipo de servicio no existe", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
        verify(notifRepo, never()).save(any());
    }

    @Test
    void createOrderWithNoExistingActivaStatus() {
        //Arrange
        UUID idCliente = UUID.randomUUID();

        CrearOrdenRequest req = new CrearOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden", 
            Instant.now());

        when(clienteRepo.findById(idCliente)).thenReturn(Optional.of(new Cliente()));
        when(prioridadRepo.findById(1)).thenReturn(Optional.of(new Prioridad()));
        when(tipoServicioRepo.findById(1)).thenReturn(Optional.of(new TipoServicio()));
        when(estadoRepo.findByNombre("ACTIVA")).thenReturn(Optional.empty());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.crear(req));

        //Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertEquals("Estado ACTIVA no configurado", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
        verify(notifRepo, never()).save(any());
    }
    
    @Test
    void editActiveOrder() {
        //Arrange
        UUID idOrden = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        var cliente = new Cliente();
        cliente.setIdCliente(idCliente);
        cliente.setNombre("Cliente 1");

        var prioridad =  new Prioridad();
        prioridad.setIdPrioridad(1);
        prioridad.setNombre("Alta");

        var tipo = new TipoServicio();
        tipo.setIdTipoServicio(1);
        tipo.setNombre("Reparacion");

        var estadoActiva = new EstadoOrden();
        estadoActiva.setIdEstado(1);
        estadoActiva.setNombre("ACTIVA");
        
        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(estadoActiva);
        ot.setEliminada(false);

        EditarOrdenRequest req = new EditarOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden actualizada", 
            Instant.now());

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.of(ot));
        when(clienteRepo.findById(idCliente)).thenReturn(Optional.of(cliente));
        when(prioridadRepo.findById(1)).thenReturn(Optional.of(prioridad));
        when(tipoServicioRepo.findById(1)).thenReturn(Optional.of(tipo));

        //Act
        serviceImpl.editar(idOrden, req);

        //Assert
        assertEquals("Orden actualizada", ot.getDescripcion());
        assertEquals(prioridad, ot.getPrioridad());
        assertEquals(cliente, ot.getCliente());
        assertEquals(tipo, ot.getTipoServicio());

        verify(ordenRepo).save(ot);
        verify(historialRepo).save(any(HistorialEstado.class));
    }

    @Test
    void editNotExistingOrder() {
        //Arrange
        UUID idOrden = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        EditarOrdenRequest req = new EditarOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden actualizada", 
            Instant.now());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.editar(idOrden, req));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Orden no encontrada", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
    }

    @Test
    void editNotActiveOrder() {
        //Arrange
        UUID idOrden = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        var estado = new EstadoOrden();
        estado.setIdEstado(1);
        estado.setNombre("CERRADA");
        
        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(estado);
        ot.setEliminada(false);

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.of(ot));

        EditarOrdenRequest req = new EditarOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden actualizada", 
            Instant.now());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.editar(idOrden, req));

        //Assert
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("La orden no se puede editar porque no está ACTIVA", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
    }

    @Test
    void editOrderWithNotExistingClient() {
        //Arrange
        UUID idOrden = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        var estado = new EstadoOrden();
        estado.setIdEstado(1);
        estado.setNombre("ACTIVA");

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(estado);
        ot.setEliminada(false);

        when(ordenRepo.findByIdAndEliminadaFalse(any(UUID.class))).thenReturn(Optional.of(ot));
        when(clienteRepo.findById(idCliente)).thenReturn(Optional.empty());

        EditarOrdenRequest req = new EditarOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden actualizada", 
            Instant.now());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.editar(idOrden, req));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Cliente no existe", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
    }

    @Test
    void editOrderWithNotExistingPriority() {
        //Arrange
        UUID idOrden = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        var estado = new EstadoOrden();
        estado.setIdEstado(1);
        estado.setNombre("ACTIVA");

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(estado);
        ot.setEliminada(false);

        when(ordenRepo.findByIdAndEliminadaFalse(any(UUID.class))).thenReturn(Optional.of(ot));
        when(clienteRepo.findById(idCliente)).thenReturn(Optional.of(new Cliente()));
        when(prioridadRepo.findById(1)).thenReturn(Optional.empty());

        EditarOrdenRequest req = new EditarOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden actualizada", 
            Instant.now());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.editar(idOrden, req));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Prioridad no existe", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
    }

    @Test
    void editOrderWithNotExistingServiceType() {
        //Arrange
        UUID idOrden = UUID.randomUUID();
        UUID idCliente = UUID.randomUUID();

        var estado = new EstadoOrden();
        estado.setIdEstado(1);
        estado.setNombre("ACTIVA");

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(estado);
        ot.setEliminada(false);

        when(ordenRepo.findByIdAndEliminadaFalse(any(UUID.class))).thenReturn(Optional.of(ot));
        when(clienteRepo.findById(idCliente)).thenReturn(Optional.of(new Cliente()));
        when(prioridadRepo.findById(1)).thenReturn(Optional.of(new Prioridad()));
        when(tipoServicioRepo.findById(1)).thenReturn(Optional.empty());

        EditarOrdenRequest req = new EditarOrdenRequest(
            idCliente, 
            1, 
            1, 
            "Orden actualizada", 
            Instant.now());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.editar(idOrden, req));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Tipo de servicio no existe", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
    }

    @Test
    void changeStatusActiveToInProcessSuccesfulWithActiveStatus() {
        //Arrange
        UUID idOrden = UUID.randomUUID();

        var activa = new EstadoOrden();
        activa.setIdEstado(1);
        activa.setNombre("ACTIVA");

        var enProceso = new EstadoOrden();
        enProceso.setIdEstado(2);
        enProceso.setNombre("EN_PROCESO");

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(activa);
        ot.setEliminada(false);

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.of(ot));
        when(estadoRepo.findByNombre("EN_PROCESO")).thenReturn(Optional.of(enProceso));

        //Act
        serviceImpl.cambiarEstado(idOrden, "EN_PROCESO", "Nota");

        //Assert
        assertEquals("EN_PROCESO", ot.getEstadoActual().getNombre());

        verify(ordenRepo).save(ot);
        verify(historialRepo).save(any(HistorialEstado.class));
    }

    @Test
    void changeStatusInProcessToClosedSuccesfulWithActiveStatusWithNullNote() {
        //Arrange
        UUID idOrden = UUID.randomUUID();

        var enProceso = new EstadoOrden();
        enProceso.setIdEstado(1);
        enProceso.setNombre("EN_PROCESO");

        var cerrada = new EstadoOrden();
        cerrada.setIdEstado(2);
        cerrada.setNombre("CERRADA");

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(enProceso);
        ot.setEliminada(false);

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.of(ot));
        when(estadoRepo.findByNombre("CERRADA")).thenReturn(Optional.of(cerrada));

        //Act
        serviceImpl.cambiarEstado(idOrden, "CERRADA", null);

        //Assert
        assertEquals("CERRADA", ot.getEstadoActual().getNombre());
        verify(ordenRepo).save(ot);
        ArgumentCaptor<HistorialEstado> captor = ArgumentCaptor.forClass(HistorialEstado.class); 
        verify(historialRepo).save(captor.capture());
        assertEquals("Cambio de estado a CERRADA", captor.getValue().getNota());
    }

    @Test
    void changeStatusSuccesfulWithInProcessStatus() {
        //Arrange
        UUID idOrden = UUID.randomUUID();

        var enProceso = new EstadoOrden();
        enProceso.setIdEstado(1);
        enProceso.setNombre("EN_PROCESO");

        var cerrada = new EstadoOrden();
        cerrada.setIdEstado(2);
        cerrada.setNombre("CERRADA");

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(enProceso);
        ot.setEliminada(false);

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.of(ot));
        when(estadoRepo.findByNombre("CERRADA")).thenReturn(Optional.of(cerrada));

        //Act
        serviceImpl.cambiarEstado(idOrden, "CERRADA", "Nota");

        //Assert
        assertEquals("CERRADA", ot.getEstadoActual().getNombre());

        verify(ordenRepo).save(ot);
        verify(historialRepo).save(any(HistorialEstado.class));
    }

    @Test
    void changeStatusWithNotExistingOrder() {
        //Arrange
        UUID idOrden = UUID.randomUUID();

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.cambiarEstado(
                                                                        idOrden, 
                                                                        "EN_PROCESO", 
                                                                        "Nota"));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Orden no encontrada", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
    }

    @Test
    void changeStatusWithInvalidTransition() {
        //Arrange
        UUID idOrden = UUID.randomUUID();

        var activa = new EstadoOrden();
        activa.setIdEstado(1);
        activa.setNombre("ACTIVA");

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(activa);
        ot.setEliminada(false);

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.of(ot));

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.cambiarEstado(
                                                                        idOrden, 
                                                                        "CERRADA", 
                                                                        "Nota"));

        //Assert
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertTrue(exception.getMessage().contains("Transición de estado no permitida"));

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
    }

    @Test
    void changeStatusWithNotExistingDestinationStatus() {
        //Arrange
        UUID idOrden = UUID.randomUUID();

        var activa = new EstadoOrden();
        activa.setIdEstado(1);
        activa.setNombre("ACTIVA");

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(activa);
        ot.setEliminada(false);

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.of(ot));
        when(estadoRepo.findByNombre("EN_PROCESO")).thenReturn(Optional.empty());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.cambiarEstado(
                                                                        idOrden, 
                                                                        "EN_PROCESO", 
                                                                        "Nota"));

        //Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Estado destino no existe", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
    }

    @Test
    void markEliminatedSuccesful() {
        //Arrange
        UUID idOrden = UUID.randomUUID();

        var activa = new EstadoOrden();
        activa.setIdEstado(1);
        activa.setNombre("ACTIVA");

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(activa);
        ot.setEliminada(false);

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.of(ot));

        //Act
        serviceImpl.marcarEliminada(idOrden, "Eliminada");

        //Assert
        assertTrue(ot.isEliminada());
        verify(ordenRepo).save(ot);
        ArgumentCaptor<HistorialEstado> captor = ArgumentCaptor.forClass(HistorialEstado.class);
        verify(historialRepo).save(captor.capture());
        assertEquals("Eliminada", captor.getValue().getNota());
    }

    @Test
    void markEliminatedSuccesfulWithNullNote() {
        //Arrange
        UUID idOrden = UUID.randomUUID();

        var activa = new EstadoOrden();
        activa.setIdEstado(1);
        activa.setNombre("ACTIVA");

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setIdOrden(idOrden);
        ot.setEstadoActual(activa);
        ot.setEliminada(false);

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.of(ot));

        //Act
        serviceImpl.marcarEliminada(idOrden, null);

        //Assert
        assertTrue(ot.isEliminada());
        verify(ordenRepo).save(ot);
        ArgumentCaptor<HistorialEstado> captor = ArgumentCaptor.forClass(HistorialEstado.class);
        verify(historialRepo).save(captor.capture());
        assertEquals("Orden marcada como eliminada", captor.getValue().getNota());
    }

    @Test
    void markEliminatedWithNotExistingOrder() {
        //Arrange
        UUID idOrden = UUID.randomUUID();

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.empty());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.marcarEliminada(
                                                                                    idOrden, 
                                                                                    "Eliminada"));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Orden no encontrada", exception.getMessage());

        verify(ordenRepo, never()).save(any());
        verify(historialRepo, never()).save(any());
    }

    @Test
    void listOrders() {
        //Arrange
        FiltroOrdenes filtro = new FiltroOrdenes(
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrdenListaItem> page = new PageImpl<>(List.of(new OrdenListaItem(
                UUID.randomUUID(),
                "00001",
                UUID.randomUUID(),
                "Ciente 1",
                "ACTIVA",
                "Alta",
                "Reparacion",
                Instant.now()
        )));

        when(ordenRepo.buscarListado(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        //Act
        var result = serviceImpl.listar(filtro, pageable);

        //Assert
        assertEquals(page, result);
        verify(ordenRepo).buscarListado(any(), any(), any(), any(), any(), any(), any(), eq(pageable));
    }

    @Test
    void detailSuccesful() {
        //Arrange
        UUID idOrden = UUID.randomUUID();
        
        var cliente = new Cliente();
        cliente.setIdCliente(UUID.randomUUID());
        cliente.setNombre("Cliente 1");

        var prioridad =  new Prioridad();
        prioridad.setIdPrioridad(1);
        prioridad.setNombre("Alta");

        var tipo = new TipoServicio();
        tipo.setIdTipoServicio(1);
        tipo.setNombre("Reparacion");

        var estado = new EstadoOrden();
        estado.setIdEstado(1);
        estado.setNombre("ACTIVA");

        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setCliente(cliente);
        ot.setPrioridad(prioridad);
        ot.setTipoServicio(tipo);
        ot.setEstadoActual(estado);

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.of(ot));

        //Act
        var result = serviceImpl.detalle(idOrden);

        //Assert
        assertNotNull(result);
    }

    @Test
    void detailWithNotExistingOrder() {
        //Arrange
        UUID idOrden = UUID.randomUUID();

        when(ordenRepo.findByIdAndEliminadaFalse(idOrden)).thenReturn(Optional.empty());

        //Act
        ApiException exception = assertThrows(ApiException.class, () -> serviceImpl.detalle(idOrden));

        //Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Orden no encontrada", exception.getMessage());
    }

    @Test
    void summaryWithNullDatesUsingDefaultsAndMappingResults() {
        //Arrange
        Instant fixedInstant = Instant.parse("2025-10-15T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        LocalDate expectedIni = LocalDate.now(fixedClock).withDayOfMonth(1);
        LocalDate expectedFin = LocalDate.now(fixedClock);

        List<Object[]> estados = List.of(new Object[] {"ACTIVA", 7L}, new Object[] {"CERRADA", 2L});
        when(ordenRepo.contarPorEstado(any(), any(), any(), any())).thenReturn(estados);

        List<Object[]> prioridades = List.of(new Object[] {"Alta", 4L}, new Object[] {"Baja", 1L});
        when(ordenRepo.conteoPorPrioridad(any(), any(), any(), any())).thenReturn(prioridades);

        List<Object[]> tipos = List.of(new Object[] {"Instalacion", 5L}, new Object[] {"Reparacion", 2L});
        when(ordenRepo.conteoPorTipoServicio(any(), any(), any(), any())).thenReturn(tipos);

        Date d1 = Date.valueOf(expectedIni);
        Date d3 = Date.valueOf(expectedIni.plusDays(2));
        List<Object[]> porDiaRaw = List.of(new Object[] { d1, 2L }, new Object[] { d3, 1L });
        when(ordenRepo.conteoPorDia(any(), any(), any(), any())).thenReturn(porDiaRaw);

        //Act
        InformeOrdenesResp resp = serviceImpl.resumen(null, null, null, null);

        //Assert
        assertNotNull(resp);
        assertEquals(expectedIni, resp.desde());
        assertEquals(expectedFin, resp.hasta());
        assertEquals(9L, resp.total());
        assertEquals(7L, resp.porEstado().get("ACTIVA").longValue());
        assertEquals(2L, resp.porEstado().get("CERRADA").longValue());
        assertEquals(4L, resp.porPrioridad().get("Alta").longValue());
        assertEquals(5L, resp.porTipoServicio().get("Instalacion").longValue());
        var mapaDias = resp.porDia()
                        .stream()
                        .collect(Collectors.toMap(
                            InformeOrdenesResp.PorDiaItem::fecha, 
                            InformeOrdenesResp.PorDiaItem::cantidad));
        assertEquals(2L, mapaDias.getOrDefault(expectedIni, 0L).longValue());
        assertEquals(1L, mapaDias.getOrDefault(expectedIni.plusDays(2), 0L).longValue());

        verify(ordenRepo).contarPorEstado(any(), any(), any(), any());
        verify(ordenRepo).conteoPorPrioridad(any(), any(), any(), any());
        verify(ordenRepo).conteoPorTipoServicio(any(), any(), any(), any());
        verify(ordenRepo).conteoPorDia(any(), any(), any(), any());
    }

    @Test
    void summaryWithDatesAndFilters() {
        //Arrange
        Instant fixedInstant = Instant.parse("2025-10-15T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        when(clock.getZone()).thenReturn(fixedClock.getZone());

        LocalDate desde = LocalDate.of(2025, 6, 1);
        LocalDate hasta = LocalDate.of(2025, 6, 3);
        UUID clienteId = UUID.randomUUID();
        Integer tipoServicioId = 42;

        List<Object[]> estados = List.of(new Object[] {"ACTIVA", 7L}, new Object[] {"CERRADA", 2L});
        when(ordenRepo.contarPorEstado(any(), any(), eq(clienteId), eq(tipoServicioId))).thenReturn(estados);

        when(ordenRepo.conteoPorPrioridad(any(), any(), eq(clienteId), eq(tipoServicioId))).thenReturn(List.of());

        when(ordenRepo.conteoPorTipoServicio(any(), any(), eq(clienteId), eq(tipoServicioId))).thenReturn(List.of());

        Date dia1 = Date.valueOf(desde);
        Date dia2 = Date.valueOf(desde.plusDays(1));
        Date dia3 = Date.valueOf(desde.plusDays(2));
        List<Object[]> porDiaRaw = List.of(new Object[] { dia1, 2L }, new Object[] { dia2, 3L }, new Object[] { dia3, 2L });
        when(ordenRepo.conteoPorDia(any(), any(), eq(clienteId), eq(tipoServicioId))).thenReturn(porDiaRaw);

        //Act
        InformeOrdenesResp resp = serviceImpl.resumen(desde, hasta, clienteId, tipoServicioId);

        //Assert
        assertNotNull(resp);
        assertEquals(desde, resp.desde());
        assertEquals(hasta, resp.hasta());
        assertEquals(9L, resp.total());
        var mapaDias = resp.porDia()
                        .stream()
                        .collect(Collectors.toMap(
                            InformeOrdenesResp.PorDiaItem::fecha, 
                            InformeOrdenesResp.PorDiaItem::cantidad));
        assertEquals(2L, mapaDias.get(desde).longValue());
        assertEquals(3L, mapaDias.get(desde.plusDays(1)).longValue());
        assertEquals(2L, mapaDias.get(desde.plusDays(2)).longValue());

        verify(ordenRepo).contarPorEstado(any(), any(), eq(clienteId), eq(tipoServicioId));
        verify(ordenRepo).conteoPorDia(any(), any(), eq(clienteId), eq(tipoServicioId));
    }
}
