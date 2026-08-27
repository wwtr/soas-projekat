# SOAS 2025/26 - Aplikacija za razmenu fiat i crypto valuta

Projekat iz predmeta Servisno orijentisana arhitektura sistema.

Aplikacija omogucava korisnicima da razmenjuju obicne (fiat) i crypto valute.
Svaki korisnik sa ulogom USER ima bankovni racun i crypto novcanik, a kursevi se
povlace sa eksternih API servisa.

## Tehnologije

Java 17, Spring Boot 3.3.4, Spring Cloud 2023.0.3, Maven, H2, Docker, React (Vite).

## Mikroservisi

| Mikroservis | Port | Opis |
|---|---|---|
| naming-server | 8761 | Eureka server |
| currency-exchange | 8000 | kursevi fiat valuta |
| currency-conversion | 8100 | razmena fiat u fiat |
| bank-account | 8200 | bankovni racuni |
| crypto-wallet | 8300 | crypto novcanici |
| crypto-exchange | 8400 | kursevi crypto valuta |
| trade-service | 8600 | razmena fiat i crypto valuta |
| api-gateway | 8765 | ulazna tacka, basic autentikacija |
| users-service | 8770 | korisnici i uloge |

Pored njih, `util` i `service-library` su zavisnosti a ne aplikacije, pa nemaju
Dockerfile ni build tag u pom.xml fajlu.

## Kredencijali

| Email | Lozinka | Uloga |
|---|---|---|
| owner@soas.rs | owner123 | OWNER |
| admin@soas.rs | admin123 | ADMIN |
| marko@soas.rs | marko123 | USER |
| jelena@soas.rs | jelena123 | USER |

Baze su in-memory, pa se ovi korisnici ponovo kreiraju pri svakom pokretanju.

## Funkcionalni URL-ovi

Svi zahtevi idu preko API Gateway-a na `http://localhost:8765` uz basic autentikaciju.
Izuzetak su kursevi, koji su javni.

### Prijava

| Metod | URL | Ko sme |
|---|---|---|
| GET | `/login` | svi prijavljeni |

### Users service

| Metod | URL | Ko sme |
|---|---|---|
| GET | `/users` | OWNER, ADMIN |
| GET | `/users/{id}` | OWNER, ADMIN |
| POST | `/users` | OWNER, ADMIN (ADMIN samo ulogu USER) |
| PUT | `/users/{id}` | OWNER, ADMIN (ADMIN samo korisnike sa ulogom USER) |
| DELETE | `/users/{id}` | OWNER |

Telo zahteva za POST i PUT:

```json
{ "email": "petar@soas.rs", "password": "petar123", "role": "USER" }
```

### Bank account

| Metod | URL | Ko sme |
|---|---|---|
| GET | `/bank-account` | ADMIN |
| GET | `/bank-account/user/{email}` | ADMIN, USER (samo svoj) |
| GET | `/bank-account/user/{email}/{currencyCode}` | ADMIN, USER (samo svoj) |
| POST | `/bank-account` | ADMIN |
| PUT | `/bank-account/{id}` | ADMIN |
| DELETE | `/bank-account/{id}` | ADMIN |

### Crypto wallet

| Metod | URL | Ko sme |
|---|---|---|
| GET | `/crypto-wallet` | ADMIN |
| GET | `/crypto-wallet/user/{email}` | ADMIN, USER (samo svoj) |
| GET | `/crypto-wallet/user/{email}/{cryptoCode}` | ADMIN, USER (samo svoj) |
| POST | `/crypto-wallet` | ADMIN |
| PUT | `/crypto-wallet/{id}` | ADMIN |
| DELETE | `/crypto-wallet/{id}` | ADMIN |

### Currency exchange i crypto exchange

| Metod | URL | Ko sme |
|---|---|---|
| GET | `/currency-exchange/from/{from}/to/{to}` | javno |
| GET | `/currency-exchange/is-fiat/{code}` | javno |
| GET | `/crypto-exchange/from/{from}/to/{to}` | javno |

### Currency conversion

| Metod | URL | Ko sme |
|---|---|---|
| GET | `/currency-conversion?from=X&to=Y&quantity=Q` | USER |

Primer: `http://localhost:8765/currency-conversion?from=EUR&to=RSD&quantity=100`

### Trade service

| Metod | URL | Ko sme |
|---|---|---|
| GET | `/trade-service?from=X&to=Y&quantity=Q` | USER |

Primer: `http://localhost:8765/trade-service?from=RSD&to=ETH&quantity=10000`

Podrzane su razmene crypto u crypto, fiat u crypto i crypto u fiat. Crypto valute
se kupuju i prodaju samo za EUR i USD, pa se svaka druga fiat valuta prvo menja
kroz currency-conversion servis.

## Pokretanje kroz Docker

```
docker compose up -d
```

Slike se nalaze na Docker Hub nalogu `wwtr35` sa `latest` tagom.

## Pokretanje iz koda

Prvo instalirati zavisnosti, redosled je bitan:

```
cd util && mvn clean install -DskipTests
cd ../service-library && mvn clean install -DskipTests
```

Zatim pokrenuti naming-server, pa ostale mikroservise sa `mvn spring-boot:run`.

## Front-end

```
cd frontend
npm install
npm run dev
```

Aplikacija se otvara na `http://localhost:5173`. Stranica sa kursevima je dostupna
bez prijave, sve ostalo trazi prijavu.

## Napomene o implementaciji

- Komunikacija izmedju mikroservisa ide iskljucivo preko Feign klijenta.
  RestTemplate je upotrebljen samo u API Gateway-u, prilikom provere kredencijala.
- Svaki servis koji cuva podatke ima svoju H2 bazu (database per service).
- Gateway posle uspesne autentikacije upisuje header-e `X-User-Email` i
  `X-User-Role`, na osnovu kojih servisi proveravaju ovlascenja. Header-i se
  postavljaju sa `set`, pa klijent ne moze da podmetne svoju ulogu.
- Rute koje sadrze `/internal` postoje samo za pozive medju servisima i gateway
  ih ne propusta spolja.
- U trade servisu su implementirani retry i circuit breaker (resilience4j).
- Global exception handler se nalazi u `util` projektu i obradjuje sve izuzetke,
  tako da korisnik nikada ne dobija stack-trace.
