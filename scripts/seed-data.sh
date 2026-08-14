#!/usr/bin/env bash
#
# seed-data.sh — Carga datos de prueba en la API (Cinetest / Cineplanet 2026)
#
# Flujo:
#   1. Espera a que la API esté disponible (healthcheck).
#   2. Crea usuarios ADMIN y CUSTOMER (si no existen).
#   3. Crea películas (ADMIN).
#   4. Crea funciones / showtimes futuros (ADMIN).
#   5. Crea reservas / bookings (CUSTOMER).
#   6. Imprime un resumen con IDs y ejemplos de consultas para probar.
#
# Uso:
#   ./scripts/seed-data.sh                    # usa http://localhost:8090
#   BASE_URL=https://retocp-backend-production.up.railway.app/ ./scripts/seed-data.sh
#
# Requiere: curl, jq

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8090}"

# ---------------------------------------------------------------------------
# Configuración de colores para salida legible.
# Todos los logs van a stderr: las funciones "create_*" usan stdout para
# devolver valores (IDs) y los mensajes no deben mezclarse con ellos.
# ---------------------------------------------------------------------------
GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[0;33m'; CYAN='\033[0;36m'
BOLD='\033[1m'; NC='\033[0m'

info()  { echo -e "${CYAN}[INFO]${NC}  $*" >&2; }
ok()    { echo -e "${GREEN}[ OK ]${NC}  $*" >&2; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*" >&2; }
fail()  { echo -e "${RED}[FAIL]${NC}  $*" >&2; exit 1; }
sep()   { echo -e "${BOLD}─────────────────────────────────────────────────────────────${NC}" >&2; }

# ---------------------------------------------------------------------------
# 1) Esperar API
# ---------------------------------------------------------------------------
sep
info "Esperando que la API esté disponible en ${BOLD}${BASE_URL}${NC} ..."
for i in $(seq 1 30); do
  if curl -s -o /dev/null "$BASE_URL/api/health"; then
    ok "API disponible."
    break
  fi
  if [[ "$i" -eq 30 ]]; then fail "La API no respondió en $BASE_URL. ¿Está levantada (docker-compose up -d)?"; fi
  sleep 2
done

# ---------------------------------------------------------------------------
# 2) Usuarios
# ---------------------------------------------------------------------------
sep
info "Paso 1/4 — Creando usuarios"

register_user() {
  local username="$1" password="$2" role="$3"
  local code
  code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$username\",\"password\":\"$password\",\"role\":\"$role\"}")
  case "$code" in
    201) ok "Usuario ${BOLD}$username${NC} (${role}) creado." ;;
    409) warn "Usuario ${BOLD}$username${NC} ya existía." ;;
    *)   warn "Register de $username devolvió $code (ignorado)." ;;
  esac
}

ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-Admin123!}"
CUSTOMER_USER="${CUSTOMER_USER:-customer}"
CUSTOMER_PASS="${CUSTOMER_PASS:-Customer123!}"

register_user "$ADMIN_USER" "$ADMIN_PASS" "ADMIN"
register_user "$CUSTOMER_USER" "$CUSTOMER_PASS" "CUSTOMER"

# ---------------------------------------------------------------------------
# 3) Login y tokens
# ---------------------------------------------------------------------------
sep
info "Paso 2/4 — Autenticando"

login_and_get_token() {
  local username="$1" password="$2"
  local body
  body=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$username\",\"password\":\"$password\"}")
  echo "$body" | jq -r '.data // empty' 2>/dev/null || echo ""
}

ADMIN_TOKEN=$(login_and_get_token "$ADMIN_USER" "$ADMIN_PASS")
CUSTOMER_TOKEN=$(login_and_get_token "$CUSTOMER_USER" "$CUSTOMER_PASS")

if [[ -z "$ADMIN_TOKEN" || -z "$CUSTOMER_TOKEN" ]]; then
  warn "No se pudo autenticar ${BOLD}$ADMIN_USER${NC} / ${BOLD}$CUSTOMER_USER${NC} con las contraseñas por defecto."
  warn "Esto suele pasar si la base de datos ya tiene esos usuarios con otra contraseña."
  warn "Solución: resetea la base de datos y vuelve a ejecutar:"
  warn "   docker-compose down -v && docker-compose up -d   (y luego ./scripts/seed-data.sh)"
  warn "También puedes indicar otras credenciales: ADMIN_USER/ADMIN_PASS/CUSTOMER_USER/CUSTOMER_PASS."
  fail "Login fallido."
fi
ok "Tokens JWT obtenidos."

# ---------------------------------------------------------------------------
# 4) Películas
# ---------------------------------------------------------------------------
sep
info "Paso 3/4 — Creando películas"

create_movie() {
  local title="$1" synopsis="$2" duration="$3" genre="$4" rating="$5"
  local body code
  body=$(curl -s -X POST "$BASE_URL/api/movies" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{\"title\":\"$title\",\"synopsis\":\"$synopsis\",\"duration\":$duration,\"genre\":\"$genre\",\"rating\":\"$rating\"}")
  code=$(echo "$body" | jq -r '.status')
  local id
  id=$(echo "$body" | jq -r '.data.id // empty')
  if [[ "$code" == "201" && -n "$id" ]]; then
    ok "Película creada: ${BOLD}$title${NC} ($genre / $rating, ${duration} min) → id=$id"
    echo "$id"
  else
    warn "No se pudo crear la película '$title' (status=$code): $(echo "$body" | jq -r '.message // empty')"
    echo ""
  fi
}

MOVIE_IDS=()
MOVIE_IDS+=("$(create_movie "Inception" "Un ladrón con acceso a los sueños ajenos." 148 "SCIENCE_FICTION" "PG_13")")
MOVIE_IDS+=("$(create_movie "The Dark Knight" "Batman enfrenta al Joker en Gotham." 152 "ACTION" "PG_13")")
MOVIE_IDS+=("$(create_movie "Parasite" "Una familia se infiltra en un hogar adinerado." 132 "THRILLER" "R")")
MOVIE_IDS+=("$(create_movie "Toy Story" "Los juguetes de Andy cobran vida." 81 "ANIMATION" "G")")
MOVIE_IDS+=("$(create_movie "La La Land" "Un músico y una actriz se enamoran en Los Ángeles." 128 "ROMANCE" "PG_13")")

FILTERED=()
for id in "${MOVIE_IDS[@]}"; do [[ -n "$id" ]] && FILTERED+=("$id"); done
if [[ ${#FILTERED[@]} -eq 0 ]]; then
  fail "No se creó ninguna película. Revisa el token de admin."
fi
info "Se cargaron ${#FILTERED[@]} películas."

# ---------------------------------------------------------------------------
# 5) Funciones / Showtimes
# ---------------------------------------------------------------------------
sep
info "Paso 4/4 — Creando funciones (showtimes futuros)"

ISO_DATETIME() { # days hours minutes
  date -d "+$1 days +$2 hours +$3 minutes" "+%Y-%m-%dT%H:%M:%S" 2>/dev/null \
    || date -v+"$1"d -v+"$2"H -v+"$3"M "+%Y-%m-%dT%H:%M:%S"
}

SHOWTIME_IDS=()
create_showtime() {
  local movie_id="$1" room="$2" days="$3" hours="$4" minutes="$5" price="$6" total="$7" available="$8"
  local dt
  dt=$(ISO_DATETIME "$days" "$hours" "$minutes")
  local body code
  body=$(curl -s -X POST "$BASE_URL/api/showtimes" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{\"movieId\":\"$movie_id\",\"room\":\"$room\",\"dateTime\":\"$dt\",\"price\":$price,\"totalSeats\":$total,\"availableSeats\":$available}")
  code=$(echo "$body" | jq -r '.status')
  local id
  id=$(echo "$body" | jq -r '.data.id // empty')
  if [[ "$code" == "201" && -n "$id" ]]; then
    ok "Showtime creado: sala $room, $dt, S/. $price → id=$id"
    echo "$id"
  else
    warn "No se pudo crear showtime (sala $room, status=$code): $(echo "$body" | jq -r '.message // empty')"
    echo ""
  fi
}

# Por cada película: 1 showtime
SHOWTIME_IDS+=("$(create_showtime "${FILTERED[0]}" "Sala 1" 1 18 30 "25.00" 100 100)")
SHOWTIME_IDS+=("$(create_showtime "${FILTERED[0]}" "Sala 1" 2 21 0 "25.00" 100 100)")
SHOWTIME_IDS+=("$(create_showtime "${FILTERED[1]}" "Sala 2" 1 20 0 "28.00" 120 120)")
SHOWTIME_IDS+=("$(create_showtime "${FILTERED[2]}" "Sala 3" 2 19 30 "22.50" 80 80)")
SHOWTIME_IDS+=("$(create_showtime "${FILTERED[3]}" "Sala 4" 3 15 0 "15.00" 200 200)")
SHOWTIME_IDS+=("$(create_showtime "${FILTERED[4]}" "Sala 5" 3 22 15 "20.00" 90 90)")

SHOW_FILTERED=()
for id in "${SHOWTIME_IDS[@]}"; do [[ -n "$id" ]] && SHOW_FILTERED+=("$id"); done
if [[ ${#SHOW_FILTERED[@]} -eq 0 ]]; then
  fail "No se creó ninguna función."
fi
info "Se cargaron ${#SHOW_FILTERED[@]} funciones."

# ---------------------------------------------------------------------------
# 6) Reservas / Bookings
# ---------------------------------------------------------------------------
sep
info "Paso final — Creando reservas (bookings)"

create_booking() {
  local showtime_id="$1" name="$2" email="$3" seats="$4"
  local body code
  body=$(curl -s -X POST "$BASE_URL/api/bookings" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $CUSTOMER_TOKEN" \
    -d "{\"showtimeId\":\"$showtime_id\",\"customerName\":\"$name\",\"customerEmail\":\"$email\",\"seatsBooked\":$seats}")
  code=$(echo "$body" | jq -r '.status')
  local id
  id=$(echo "$body" | jq -r '.data.id // empty')
  if [[ "$code" == "201" && -n "$id" ]]; then
    ok "Reserva creada para $name ($seats asientos) → id=$id"
    echo "$id"
  else
    warn "No se pudo crear reserva (status=$code): $(echo "$body" | jq -r '.message // empty')"
    echo ""
  fi
}

BOOKING_IDS=()
BOOKING_IDS+=("$(create_booking "${SHOW_FILTERED[0]}" "Juan Pérez" "juan.perez@test.com" 2)")
BOOKING_IDS+=("$(create_booking "${SHOW_FILTERED[1]}" "María Gómez" "maria.gomez@test.com" 4)")
BOOKING_IDS+=("$(create_booking "${SHOW_FILTERED[2]}" "Carlos Ruiz" "carlos.ruiz@test.com" 1)")

BOOK_FILTERED=()
for id in "${BOOKING_IDS[@]}"; do [[ -n "$id" ]] && BOOK_FILTERED+=("$id"); done

# ---------------------------------------------------------------------------
# Resumen y ejemplos
# ---------------------------------------------------------------------------
sep
echo -e "${BOLD}${GREEN}Resumen de la carga de datos:${NC}"
echo -e "  Películas : ${#FILTERED[@]}"
echo -e "  Funciones : ${#SHOW_FILTERED[@]}"
echo -e "  Reservas  : ${#BOOK_FILTERED[@]}"
echo ""
echo -e "${BOLD}IDs de utilidad:${NC}"
[[ -n "${FILTERED[0]:-}" ]] && echo "  movieId    : ${FILTERED[0]}"
[[ -n "${SHOW_FILTERED[0]:-}" ]] && echo "  showtimeId : ${SHOW_FILTERED[0]}"
[[ -n "${BOOK_FILTERED[0]:-}" ]] && echo "  bookingId  : ${BOOK_FILTERED[0]}"
echo ""
echo -e "${BOLD}Ejemplos para probar:${NC}"
echo "  # Login como admin y copia el token"
echo "  curl -s -X POST $BASE_URL/api/auth/login -H 'Content-Type: application/json' \\"
echo "    -d '{\"username\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASS\"}'"
echo ""
echo "  # Listar películas (filtro por género/clasificación)"
echo "  curl -s \"$BASE_URL/api/movies?genre=SCIENCE_FICTION&rating=PG_13\""
echo ""
echo "  # Listar funciones con filtros"
echo "  curl -s \"$BASE_URL/api/showtimes?page=0&size=10\""
echo ""
echo "  # Detalle de una película (incluye funciones próximas)"
echo "  curl -s \"$BASE_URL/api/movies/${FILTERED[0]}\""
echo ""
echo "  # Consultar una reserva (requiere token)"
echo "  curl -s \"$BASE_URL/api/bookings/${BOOK_FILTERED[0]:-<booking-id>}\" -H 'Authorization: Bearer <token>'"
echo ""
echo "  # Swagger UI (documentación interactiva)"
echo "  $BASE_URL/api/docs"
sep
info "Listo. Disfruta probando la API 🎬"
