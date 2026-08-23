# SOAS 2025/26 - Aplikacija za razmenu fiat i crypto valuta

Projekat iz predmeta Servisno orijentisana arhitektura sistema.

## Status

U izradi. Dokumentacija sa svim URL-ovima i kredencijalima bice popunjena
pred predaju projekta.

## Mikroservisi

| Mikroservis | Port |
|---|---|
| naming-server | 8761 |
| currency-exchange | 8000 |
| currency-conversion | 8100 |
| bank-account | 8200 |
| crypto-wallet | 8300 |
| crypto-exchange | 8400 |
| trade-service | 8600 |
| api-gateway | 8765 |
| users-service | 8770 |

## Zavisnosti

- `util` - globalno upravljanje izuzecima
- `service-library` - DTO objekti, Feign proxy-ji i pomocne komponente
