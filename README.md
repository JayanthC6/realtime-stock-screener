# Real-Time Stock Screener & Alert Engine

A production-grade real-time stock screening platform built with Spring Boot, Redis Pub/Sub, WebSockets, React, and PostgreSQL - designed to analyze 500+ stocks against configurable screening indicators and deliver instant alerts to users.

---

## What This Project Does

Most stock screeners are static - you refresh the page to see updated data. This platform is different:

- **Live stock prices** stream from Finnhub WebSocket feed into the backend
- **Screening engine** evaluates every stock against P/E Ratio, RSI, and Volume indicators every 5 seconds
- **Alert engine** checks user-configured rules on every market refresh and pushes instant notifications
- **React dashboard** updates in real-time via WebSocket - no page refresh needed

---

## Architecture
Finnhub WebSocket Feed
↓
Spring Boot (WebSocket Client)
↓
Redis Pub/Sub (stock-updates channel)
↓
Spring Boot (Screening Engine + Alert Engine)
↓                    ↓
PostgreSQL              React Frontend
(rules, alerts,     (Live dashboard via
history)            WebSocket)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2 |
| Real-time | WebSocket, Redis Pub/Sub |
| Database | PostgreSQL, Spring Data JPA, Hibernate |
| Security | Spring Security, JWT |
| Frontend | React.js, Tailwind CSS |
| External API | Finnhub WebSocket + REST API |
| DevOps | Docker, Docker Compose |

---

## Core Features

### Live Stock Dashboard
- Real-time price updates for 500+ US stocks via Finnhub WebSocket
- Price, volume, and percentage change displayed live
- No polling - pure WebSocket push from server to browser

### Screening Engine
Evaluates 3 configurable indicators every 5 seconds:
- **P/E Ratio** - filters overvalued or undervalued stocks
- **RSI (14-period)** - identifies overbought (>70) or oversold (<30) conditions
- **Volume** - flags unusual trading activity against average volume

### Alert Engine
- Users configure personalized alert rules (e.g. "Alert me when AAPL RSI > 70")
- Rules stored in PostgreSQL
- Engine evaluates all rules on every market refresh cycle
- Triggered alerts delivered instantly via Redis Pub/Sub → WebSocket → browser notification
- Full alert history stored and viewable

### REST APIs (10+)
- Auth: Register, Login (JWT)
- Stocks: Get all stocks, Get single stock detail
- Screener: Get latest screener results, filter by indicator
- Alerts: Create rule, Get rules, Delete rule, Get alert history

### JWT Authentication
- Secure endpoints with JWT tokens
- Role-based access for future extensibility

---

## What You'll See When You Run It

1. **Login/Register** - JWT secured auth screen
2. **Live Dashboard** - Stock prices ticking in real-time on screen
3. **Screener Tab** - Table of stocks filtered by P/E, RSI, Volume thresholds you set
4. **Alerts Tab** - Configure your alert rules, watch notifications appear instantly when triggered
5. **Alert History** - Log of every alert that was triggered with timestamp

---

## Project Structure
realtime-stock-screener/
├── backend/
│   ├── src/main/java/com/stockscreener/
│   │   ├── StockScreenerApplication.java
│   │   ├── config/          # WebSocket, Redis, Security config
│   │   ├── controller/      # REST API controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── model/           # JPA entities
│   │   ├── dto/             # Request/Response DTOs
│   │   ├── websocket/       # Finnhub WebSocket client + STOMP handler
│   │   ├── redis/           # Publisher and Subscriber
│   │   ├── screening/       # Screening engine + indicator calculators
│   │   ├── alert/           # Alert engine + rule evaluator
│   │   └── security/        # JWT filter, UserDetails
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Dashboard/
│   │   │   ├── Screener/
│   │   │   ├── Alerts/
│   │   │   └── Auth/
│   │   ├── services/        # API calls + WebSocket service
│   │   ├── hooks/           # useWebSocket custom hook
│   │   └── context/         # Auth context
│   └── .env
├── docker-compose.yml
├── .gitignore
└── README.md

---

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL running locally
- Redis running locally
- Finnhub API key (free at finnhub.io)

### Backend Setup
```bash
cd backend
# Add your Finnhub API key and DB credentials to application.properties
mvn clean install
mvn spring-boot:run
```

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

### Docker Setup (recommended)
```bash
docker-compose up --build
```

---

## Environment Variables

### Backend (`application.properties`)
finnhub.api.key=your_finnhub_api_key
spring.datasource.password=yourpassword
jwt.secret=your_jwt_secret

### Frontend (`.env`)
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
---

## Key Engineering Decisions

**Why Redis Pub/Sub?**
Decouples the Finnhub data ingestion layer from the screening and alert engine. If the screener is slow, it doesn't block incoming market data.

**Why WebSocket over polling?**
Polling every second for 500+ stocks is wasteful and creates unnecessary DB load. WebSocket push means the client only receives data when something actually changes.

**Why calculate RSI in-house?**
Finnhub free tier doesn't provide RSI. Calculating 14-period RSI from price history gives full control over the indicator logic and avoids paid API dependency.

---

## Resume Highlights This Project Demonstrates
- Real-time event-driven architecture (WebSocket + Redis Pub/Sub)
- Spring Boot backend with async processing and scheduled jobs
- JWT-secured REST APIs with Spring Security
- PostgreSQL-backed rule management and alert history
- Live React dashboard with WebSocket integration
- Docker Compose for multi-container orchestration
