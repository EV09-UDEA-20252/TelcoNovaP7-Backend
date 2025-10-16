CREATE TABLE IF NOT EXISTS usuario (
  id_usuario UUID PRIMARY KEY,
  nombre TEXT NOT NULL,
  numero_iden TEXT NOT NULL UNIQUE,
  email TEXT NOT NULL UNIQUE,
  telefono TEXT NOT NULL,
  rol TEXT NOT NULL,
  password_hash TEXT NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS cliente (
  id_cliente UUID PRIMARY KEY,
  nombre TEXT NOT NULL,
  identificacion TEXT NOT NULL UNIQUE,
  telefono TEXT NOT NULL,
  email TEXT,
  pais TEXT,
  departamento TEXT,
  ciudad TEXT,
  direccion TEXT NOT NULL,
  creado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tipo_servicio (
  id_tipo_servicio SMALLINT PRIMARY KEY,
  nombre TEXT NOT NULL UNIQUE,
  sla_horas INT
);

CREATE TABLE IF NOT EXISTS prioridad (
  id_prioridad SMALLINT PRIMARY KEY,
  nombre TEXT NOT NULL UNIQUE,
  peso INT NOT NULL
);

CREATE TABLE IF NOT EXISTS estado_orden (
  id_estado SMALLINT PRIMARY KEY,
  nombre TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS orden_trabajo (
  id_orden UUID PRIMARY KEY,
  nro_orden TEXT NOT NULL UNIQUE,
  id_cliente UUID NOT NULL,
  id_tipo_servicio SMALLINT NOT NULL,
  id_prioridad SMALLINT NOT NULL,
  id_estado_actual SMALLINT NOT NULL,
  descripcion TEXT,
  creada_por UUID NOT NULL,
  creada_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizada_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  programada_en TIMESTAMP WITH TIME ZONE,
  cerrada_en TIMESTAMP WITH TIME ZONE,
  CONSTRAINT fk_ot_cliente        FOREIGN KEY (id_cliente)       REFERENCES cliente(id_cliente)           ON DELETE RESTRICT,
  CONSTRAINT fk_ot_tipo_servicio  FOREIGN KEY (id_tipo_servicio) REFERENCES tipo_servicio(id_tipo_servicio) ON DELETE RESTRICT,
  CONSTRAINT fk_ot_prioridad      FOREIGN KEY (id_prioridad)     REFERENCES prioridad(id_prioridad)       ON DELETE RESTRICT,
  CONSTRAINT fk_ot_estado_actual  FOREIGN KEY (id_estado_actual) REFERENCES estado_orden(id_estado)       ON DELETE RESTRICT,
  CONSTRAINT fk_ot_creada_por     FOREIGN KEY (creada_por)       REFERENCES usuario(id_usuario)           ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS historial_estado (
  id_hist UUID PRIMARY KEY,
  id_orden UUID NOT NULL,
  id_estado SMALLINT NOT NULL,
  cambiado_por UUID NOT NULL,
  cambiado_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  nota TEXT,
  CONSTRAINT fk_he_orden   FOREIGN KEY (id_orden)   REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE,
  CONSTRAINT fk_he_estado  FOREIGN KEY (id_estado)  REFERENCES estado_orden(id_estado) ON DELETE RESTRICT,
  CONSTRAINT fk_he_usuario FOREIGN KEY (cambiado_por) REFERENCES usuario(id_usuario)   ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS notificacion (
  id_notif UUID PRIMARY KEY,
  id_orden UUID NOT NULL,
  destinatario_tipo TEXT NOT NULL,
  destinatario_id UUID,
  canal TEXT NOT NULL,
  plantilla TEXT,
  enviado_en TIMESTAMP WITH TIME ZONE,
  estado_envio TEXT NOT NULL DEFAULT 'PENDIENTE',
  detalle TEXT,
  CONSTRAINT fk_notif_ot FOREIGN KEY (id_orden) REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE
);


MERGE INTO estado_orden (id_estado, nombre) KEY(id_estado) VALUES
  (1,'REGISTRADA'), (2,'ASIGNADA'), (3,'EN_PROCESO'), (4,'RESUELTA'), (5,'CERRADA');

MERGE INTO prioridad (id_prioridad, nombre, peso) KEY(id_prioridad) VALUES
  (1,'BAJA',1), (2,'MEDIA',2), (3,'ALTA',3);

MERGE INTO tipo_servicio (id_tipo_servicio, nombre, sla_horas) KEY(id_tipo_servicio) VALUES
  (1,'INSTALACION',48), (2,'REPARACION',24), (3,'MANTENIMIENTO',72);

-- Cliente de ejemplo
MERGE INTO cliente (id_cliente, nombre, identificacion, telefono, email, pais, departamento, ciudad, direccion)
  KEY(id_cliente) VALUES
  (RANDOM_UUID(), 'Cliente Demo', 'C-123', '3100000000', 'cliente@demo.local', 'CO', 'ANTIOQUIA', 'MEDELLIN', 'Calle 123 #45-67');
