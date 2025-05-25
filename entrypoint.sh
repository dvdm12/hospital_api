#!/usr/bin/env sh
set -e

# Espera a que la base de datos esté lista
until nc -z db 3306; do
  echo "Esperando a que MySQL levante..."
  sleep 2
done

# Arranca la app con opciones Java
exec java $JAVA_OPTS -jar /app/app.jar