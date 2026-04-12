# Farm Produce — Quality Grading & Wholesale Procurement

Full-stack demo for managing farm produce from farmer submission through quality inspection, grading, procurement orders, and warehouse-style inventory. **Backend:** Spring Boot 3.2 + JPA + MySQL. **Frontend:** React 19 + Vite + React Router + Tailwind CSS 4.

---

## What the application does

1. **Farmers** register lots (category, quantity, unit, harvest date). Produce moves through statuses: `SUBMITTED` → `UNDER_INSPECTION` → `GRADED` (after inspector approval) or `REJECTED`.
2. **Quality inspectors** can mark produce under inspection, create a one-to-one inspection assignment, record a score (0–100), then approve or reject. Approved inspections set produce to **`GRADED`** and attach a **quality grade** from DB score bands.
3. **Procurement officers** create orders only for **`GRADED`** produce, with quantity ≤ remaining lot quantity and matching **unit type**. Orders: `CREATED` → `APPROVED` → `COMPLETED` (inventory increases by category) or `CANCELLED`.
4. **Admins** browse users, produce, inspections, inventory, and orders (read-focused UI).
5. **Login** is MVP-style: pick a role and a seeded user (no password). Session is stored in `localStorage`.

On first startup, **`SeedDataLoader`** can seed categories, users, quality grades (with score ranges), optional demo graded lot, and sample inventory rows (see `backend/.../config/SeedDataLoader.java`).

---

## Repository layout

| Path | Purpose |
|------|---------|
| `backend/` | Spring Boot API (`farm-produce-api`), Maven project |
| `backend/src/main/java/com/farmproduce/` | Java packages: `entity`, `enums`, `repository`, `service`, `controller`, `config` |
| `backend/src/main/resources/application.properties` | Datasource, JPA, server port |
| `frontend/` | Vite + React SPA |
| `frontend/src/pages/` | `LoginPage`, `FarmerDashboard`, `InspectorDashboard`, `ProcurementDashboard`, `AdminDashboard` |
| `frontend/src/api/farmApi.js` | HTTP helpers calling `/api/...` |
| `frontend/src/session.js` | `localStorage` session keys |
| `frontend/.env.example` | Copy to `.env` for `VITE_API_BASE` |

Optional / misc: `COMMIT_MSGS.txt`, `frontend/src/misc/`, `frontend/src/stuff/` (non-core helpers).

---

## Backend (Java / Spring Boot)

### Stack

- Java **17**
- Spring Boot **3.2.5** (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`)
- MySQL via **`mysql-connector-j`**
- **`commons-lang3`** (utility use in app/seed)

### Main entry

- **`com.farmproduce.FarmProduceApplication`** — Spring Boot bootstrap.

### Layers (conceptual)

- **Entities** (`entity/`): JPA models — `User`, `ProduceCategory`, `FarmProduce`, `QualityGrade`, `QualityInspection`, `ProcurementOrder`, `ProduceInventory`. JSON uses names like `produceStatus`, `categoryName`, `procurementOfficer`, `orderDate`, etc., where configured.
- **Enums** (`enums/`): `UserRole`, `ProduceStatus`, `UnitType`, `InspectionStatus`, `ProcurementStatus`, `InventoryStatus`.
- **Repositories** (`repository/`): Spring Data JPA interfaces.
- **Services** (`service/`): Business rules — e.g. `ProcurementService` (only `GRADED` produce, quantity checks, totals, inventory on complete), `QualityInspectionService` (assign → score → approve/reject), `FarmProduceService`, `ProduceInventoryService`, `ProduceCategoryService`, `UserService`.
- **Controllers** (`controller/`): REST under **`/api/...`** — `UserController`, `ProduceCategoryController`, `FarmProduceController`, `QualityInspectionController`, `ProcurementController`, `ProduceInventoryController`, plus `GlobalExceptionHandler`.
- **Config** (`config/`): `CorsConfig` (allows browser origin for Vite), `SeedDataLoader` (`CommandLineRunner`).

### REST API (high level)

| Base path | Role |
|-----------|------|
| `/api/users` | CRUD users |
| `/api/categories` | Produce categories |
| `/api/produce` | Farm produce (query `farmerId`, `status`; PUT `.../under-inspection`) |
| `/api/inspections` | Inspections; PUT `.../inspect`, `.../approve`, `.../reject` |
| `/api/procurement` | Procurement orders; approve/complete/cancel routes |
| `/api/inventory` | Category-level inventory |

Default API port: **8080** (`server.port` in `application.properties`).

---

## Database (MySQL)

- **Database name:** `farm_produce` (URL includes `createDatabaseIfNotExist=true` so Hibernate/MySQL can create it if missing).
- **Defaults:** user `root`, password `root` unless overridden by environment variables.
- **Schema:** JPA **`ddl-auto=update`** applies schema changes from entities (good for dev; use migrations for production).

### Environment variables (backend)

| Variable | Purpose | Default in `application.properties` |
|----------|---------|--------------------------------------|
| `MYSQL_USER` | MySQL username | `root` |
| `MYSQL_PASSWORD` | MySQL password | `root` |

Example:

```bash
export MYSQL_USER=root
export MYSQL_PASSWORD=your_mysql_password
```

### Optional SQL (older data)

If you upgraded from a build that used `APPROVED` on produce:

```sql
UPDATE farm_produce SET status = 'GRADED' WHERE status = 'APPROVED';
```

If `order_date` was null on old procurement rows:

```sql
UPDATE procurement_orders SET order_date = UTC_TIMESTAMP() WHERE order_date IS NULL;
```

---

## Frontend (React / Vite)

### Stack

- React **19**, Vite **8**, React Router **7**, Tailwind **4** (`@tailwindcss/vite`).

### Routes

| Path | Page |
|------|------|
| `/` | Login (role + user picker) |
| `/farmer` | Farmer dashboard |
| `/inspector` | Inspector dashboard |
| `/procurement` | Procurement dashboard |
| `/admin` | Admin dashboard |

### Config

- **`VITE_API_BASE`**: Base URL for the API (default in code falls back to `http://localhost:8080` if unset).
- Copy **`frontend/.env.example`** → **`frontend/.env`** and adjust if the API is not on port 8080.

Vite dev server default: **http://localhost:5173** (unless you use a custom script/port).

---

## Commands — run everything (by layer)

Run from the **repository root** unless a step says otherwise.

### 1. Prerequisites

- **JDK 17** (`java -version`, `javac -version`)
- **Maven 3** (`mvn -version`)
- **Node.js 18+** (or 20+) and **npm** (`node -v`, `npm -v`)
- **MySQL 8** server running locally (or reachable host), with credentials matching env/properties

### 2. Database layer — ensure MySQL is running

macOS (Homebrew) example:

```bash
brew services start mysql
# or
mysql.server start
```

Log in to verify (password as you configured):

```bash
mysql -u root -p -e "SELECT 1;"
```

### 3. Backend — install dependencies and run

```bash
cd backend
mvn -q -DskipTests dependency:resolve
mvn spring-boot:run
```

Or build a JAR and run:

```bash
cd backend
mvn -q -DskipTests package
java -jar target/farm-produce-api-0.0.1-SNAPSHOT.jar
```

**Compile only (CI check):**

```bash
cd backend
mvn -q -DskipTests compile
```

**Run tests:**

```bash
cd backend
mvn test
```

API should log startup and listen on **http://localhost:8080**.

### 4. Frontend — install and dev server

```bash
cd frontend
cp .env.example .env   # optional; edit VITE_API_BASE if needed
npm install
npm run dev
```

Open **http://localhost:5173** (or the URL Vite prints).

**Production build and local preview:**

```bash
cd frontend
npm run build
npm run preview
```

**Lint:**

```bash
cd frontend
npm run lint
```

**Alternate dev port (script already in package.json):**

```bash
cd frontend
npm run start2
```

### 5. Typical local workflow (all layers)

**Terminal 1 — MySQL:** ensure service is up (see section 2).

**Terminal 2 — Backend:**

```bash
cd backend
export MYSQL_PASSWORD=your_password   # if not using default root/root
mvn spring-boot:run
```

**Terminal 3 — Frontend:**

```bash
cd frontend
npm install
npm run dev
```

Then: open the app → login as seeded roles (e.g. farmer → inspector → procurement) and walk through produce → inspection → procurement → complete order.

---

## Troubleshooting

| Issue | What to check |
|-------|----------------|
| `Access denied for user 'root'@'localhost'` | `MYSQL_PASSWORD` / MySQL user matches `application.properties` |
| Port **8080** in use | Stop other Java processes or change `server.port` |
| Frontend cannot reach API | `VITE_API_BASE` in `frontend/.env`; CORS in `CorsConfig.java`; backend running |
| Empty users / no seed | DB was wiped; restart app with empty tables so `SeedDataLoader` runs (see its conditions) |
| Produce stuck on old status | Run optional SQL in [Database](#database-mysql) section |

---

## Seeded demo users (when `users` table is empty)

| Email | Role |
|-------|------|
| `admin@farm.local` | `ADMIN` |
| `farmer@farm.local` | `FARMER` |
| `inspector@farm.local` | `QUALITY_INSPECTOR` |
| `procurement@farm.local` | `PROCUREMENT_OFFICER` |

---

## License / credits

Internal / educational project structure. Adjust license and attribution as needed for your course or deployment.
