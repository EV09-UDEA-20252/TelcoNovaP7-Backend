CREATE TABLE usuario (
    id_usuario UUID PRIMARY KEY,
    nombre TEXT NOT NULL,
    numero_iden TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    telefono TEXT NOT NULL,
    rol TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE cliente (
    id_cliente UUID PRIMARY KEY,
    nombre TEXT NOT NULL,
    identificacion TEXT NOT NULL UNIQUE,
    telefono TEXT NOT NULL,
    email TEXT,
    pais TEXT,
    departamento TEXT,
    ciudad TEXT,
    direccion TEXT NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tipo_servicio(
    id_tipo_servicio SMALLSERIAL PRIMARY KEY,
    nombre TEXT NOT NULL UNIQUE,
    sla_horas INT
);

CREATE TABLE prioridad(
    id_prioridad SMALLSERIAL PRIMARY KEY,
    nombre TEXT NOT NULL UNIQUE,
    peso INT NOT NULL
);

CREATE TABLE estado_orden(
    id_estado SMALLSERIAL PRIMARY KEY,
    nombre text NOT NULL UNIQUE
);

CREATE TABLE orden_trabajo(
    id_orden UUID PRIMARY KEY,
    nro_orden TEXT NOT NULL UNIQUE,
    id_cliente UUID NOT NULL REFERENCES cliente(id_cliente) ON DELETE RESTRICT,
    id_tipo_servicio SMALLINT NOT NULL REFERENCES tipo_servicio(id_tipo_servicio) ON DELETE RESTRICT,
    id_prioridad SMALLINT NOT NULL REFERENCES prioridad(id_prioridad) ON DELETE RESTRICT,
    id_estado_actual SMALLINT NOT NULL REFERENCES estado_orden(id_estado) ON DELETE RESTRICT,
    descripcion TEXT,
    creada_por UUID NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizada_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    programada_en TIMESTAMPTZ,
    cerrada_en TIMESTAMPTZ
);

CREATE TABLE historial_estado(
    id_hist UUID PRIMARY KEY,
    id_orden UUID NOT NULL REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE,
    id_estado SMALLINT NOT NULL REFERENCES estado_orden(id_estado) ON DELETE RESTRICT,
    cambiado_por UUID NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    cambiado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    nota TEXT
);

CREATE TABLE notificacion(
    id_notif UUID PRIMARY KEY,
    id_orden UUID NOT NULL REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE,
    destinatario_tipo TEXT NOT NULL,
    destinatario_id UUID,
    canal TEXT NOT NULL,
    plantilla TEXT,
    enviado_en TIMESTAMPTZ,
    estado_envio TEXT NOT NULL DEFAULT 'PENDIENTE',
    detalle TEXT
);

CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.actualizada_en := NOW();
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS orden_trabajo_touch ON orden_trabajo;
CREATE TRIGGER orden_trabajo_touch
BEFORE UPDATE ON orden_trabajo
FOR EACH ROW EXECUTE FUNCTION set_updated_at();