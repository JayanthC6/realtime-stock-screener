# 📈 Real-Time Stock Screener

A full-stack application that streams live stock prices from Finnhub's WebSocket API, computes RSI in real time, and alerts users when their configured conditions are triggered.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        DATA INGESTION                           │
│                                                                 │
│  Finnhub WebSocket ──► FinnhubWebSocketClient (single conn)     │
│       (50 symbols)           │                                  │
│                              ▼                                  │
│                    StockDataService                             │
│                   ┌──────────┴──────────┐                       │
│                   ▼                     ▼                       │
│           price_history table     stock_data table              │
│           (every tick)            (latest snapshot)             │
│                   │                     │                       │
│                   └──────────┬──────────┘                       │
│                              ▼                                  │
│                    RSI-14 Calculation                           │
│                    (from last 15 DB ticks)                      │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      REAL-TIME PIPELINE                         │
│                                                                 │
│  StockDataPublisher ──► Redis Pub/Sub ──► StockDataSubscriber   │
│                          (stock-updates)         │              │
│                                                  ▼              │
│                                        ScreeningEngine          │
│                                        ├─ RSI overbought (>70)  │
│                                        ├─ RSI oversold  (<30)   │
│                                        ├─ Volume spike          │
│                                        └─ P/E alert (>25)       │
│                                                  │              │
│                                                  ▼              │
│                                     STOMP WebSocket Broker      │
│                                       /topic/screener           │
│                                       /user/{email}/queue/alerts│
└──────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (React)                         │
│                                                                 │
│  Dashboard ──► StockList  (live prices, RSI, P/E)              │
│           ├──► Screener   (triggered alert conditions)          │
│           └──► Alerts     (user-configured alert rules)         │
│                                                                 │
│  Click any stock ──► StockDetailModal (live area chart)         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2 |
| Security | Spring Security, JWT (jjwt 0.11.5) |
| Real-time | Finnhub WebSocket, Spring WebSocket / STOMP |
| Caching | Redis (Docker) — 5-second TTL on stock reads |
| Messaging | Redis Pub/Sub |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA / Hibernate |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Frontend | React 19, Vite, Recharts, TailwindCSS |

---

## ⚙️ Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 15 running on port 5432
- Docker (for Redis)

### 1. Start Redis
```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

### 2. Create PostgreSQL database
```sql
CREATE DATABASE stockscreener;
```

### 3. Configure environment variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp backend/.env.example backend/.env
```

| Variable | Description | Required |
|---|---|---|
| `FINNHUB_API_KEY` | Your Finnhub API key from [finnhub.io](https://finnhub.io) | Yes |
| `JWT_SECRET` | Base64-encoded secret (min 256 bits) | Yes |

> **Default fallback:** If env vars are not set, the app uses the development defaults from `application.properties`. This is fine locally but **never deploy without setting real secrets**.

### 4. Run the backend
```bash
cd backend
mvn clean spring-boot:run
```

Backend starts on **http://localhost:8081**

### 5. Run the frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend starts on **http://localhost:5173**

---

## 📡 API Reference

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | ❌ | Register new user |
| POST | `/api/auth/login` | ❌ | Login, get JWT token |
| GET | `/api/stocks?page=0&size=20` | ❌ | Paginated stock list |
| GET | `/api/stocks/all` | ❌ | All stocks (no pagination) |
| GET | `/api/stocks/{symbol}` | ❌ | Single stock snapshot |
| GET | `/api/stocks/{symbol}/history?hours=1` | ❌ | Price history for chart |
| GET | `/api/stocks/screener/results` | ❌ | All screener evaluations |
| GET | `/api/stocks/screener/triggered` | ❌ | Triggered alerts only |
| POST | `/api/alerts` | ✅ JWT | Create alert rule |
| GET | `/api/alerts` | ✅ JWT | Get user's alert rules |
| DELETE | `/api/alerts/{id}` | ✅ JWT | Delete alert rule |
| GET | `/api/alerts/history` | ✅ JWT | Alert trigger history |

### Interactive API Docs (Swagger UI)
```
http://localhost:8081/swagger-ui.html
```

---

## 🧪 Running Tests

```bash
cd backend
mvn test
```

Test coverage:
- `StockDataServiceTest` — RSI calculation, price update logic, exception handling
- `JwtServiceTest` — token generation, extraction, validation, expiry
- `GlobalExceptionHandlerTest` — 404/403/500 structured JSON responses

---

## 📊 Key Features

- **Live WebSocket feed** — Single Finnhub WebSocket connection streams 50 large-cap stocks and cryptocurrencies (e.g. BTCUSDT) for 24/7 testing
- **Real-Time Frontend Search** — Instantly filter through live stocks and crypto without reloading
- **RSI-14 Calculation** — Computed from the last 15 DB ticks per symbol (survives restarts)
- **Intraday High/Low/Open** — Tracked per symbol from tick data
- **Price History Table** — Every tick is persisted; use `/history?hours=N` for chart data
- **Redis Caching** — 5-second TTL on individual stock reads reduces DB load
- **Real-time Screener** — Evaluates RSI overbought/oversold, volume spikes, P/E ratio on every tick
- **User Alerts** — Configure custom alert rules, notified via WebSocket
- **JWT Auth** — Stateless authentication, all user-specific APIs secured
- **Swagger UI** — Full interactive API documentation with bearer auth

---

## 🗂️ Project Structure

```
realtime-stock-screener/
├── backend/
│   ├── src/
│   │   ├── main/java/com/stockscreener/
│   │   │   ├── config/          # CORS, Redis, Security, Swagger, WebSocket configs
│   │   │   ├── controller/      # REST endpoints
│   │   │   ├── dto/             # Request/Response objects
│   │   │   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│   │   │   ├── model/           # JPA entities (StockData, PriceHistory, User, AlertRule)
│   │   │   ├── redis/           # Pub/Sub publisher and subscriber
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   ├── screening/       # Real-time screener engine
│   │   │   ├── security/        # JWT filter and service
│   │   │   ├── service/         # Business logic
│   │   │   └── websocket/       # Finnhub WebSocket client
│   │   └── test/               # JUnit 5 + Mockito unit tests
│   └── .env.example
└── frontend/
    └── src/
        ├── components/
        │   ├── Auth/            # Login, Register
        │   ├── Dashboard/       # StockList, StockDetailModal, Dashboard
        │   ├── Screener/        # Screener view
        │   └── Alerts/          # Alert rules management
        ├── services/            # axios API client, WebSocket client
        ├── context/             # AuthContext (JWT state)
        └── hooks/               # useWebSocket
```
