Pueden usar el modo dev (se puede cambiar al pgadmin en aplication.propieties comentando la linea de spring.profiles.active)
En el modo dev se usa H2 con 3 usuarios inicializados gracias al DevSeedConfig.java presente en la carpeta de config
sus credenciales son:

para Admin:
correo: 'admin@acme.com'
contraseña: 'admin123'

para Operario:
correo: 'operario@acme.com'
contraseña: 'operario123'

para Tecnico:
correo: 'tecnico@acme.com'
contraseña: 'tecnico123'