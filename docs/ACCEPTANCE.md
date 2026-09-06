# Dollar Ledger v1 acceptance contract

Repository: wangqiyue26-lab/Bookkeeping. Native Kotlin / Compose / Material 3.
Application ID com.local.bookkeeping; minSdk 26; compile/target SDK 36; version 1.0.0 (1).
Cloud development only. User has Android phone; never require local setup.

## Financial invariants
- CNY is the source of truth, stored as Long cents; rates are decimal strings.
- USD = CNY * rate, rounded HALF_UP to 2 places.
- Transaction saves its CNY amount, historical rate/date and USD amount.
- Editing amount uses the original rate unless user explicitly chooses latest.
- Current account = opening CNY + income CNY - expense CNY.
- Current USD = current CNY * current rate, never sum historical USD.
- Total excludes archived accounts. Statistics sum historical transaction USD.

## Required features
- Default Checking, Savings, Cash accounts with zero opening, no fake transactions.
- Create/edit/archive accounts; delete only empty accounts.
- Income/expense create/edit/delete with confirmation; dates, category, account, notes.
- Home total, account cards, quick actions, 7 recent transactions.
- Transactions grouped by date; month/type/account/category filtering.
- Account detail with month filtering; full transaction details.
- Statistics month income/expense/net and category Canvas chart/bars.
- Frankfurter v2 GET /v2/rate/CNY/USD; 6-hour persisted cache.
- Cache displays immediately; failures preserve cache; first offline supports manual rate.
- Manual mode never overwritten by network. Disable manual fetches current online rate.
- Settings in DataStore: manual rate/mode, CNY secondary display, theme, default account.
- Room stores accounts, transactions, exchange cache; Flow -> repository -> ViewModel -> UI.
- SAF JSON schema validation and replace confirmation; export/import settings and rates.
- CSV transaction export; confirm delete all and recreate defaults.
- System/light/dark themes, original adaptive icon and SplashScreen API.
- INTERNET only; no tracking, login, cloud sync, payment or sensitive permissions.
- Navy/blue original banking visual language; accessible controls >=48dp.
- Friendly errors, strict positive amount validation, duplicate save prevention.

## Delivery gates
First minimal Compose CI success before feature expansion.
Each major stage committed; use codex/develop PR after initialization.
Android Build: push/main, PR/main, workflow_dispatch; JDK17, Gradle wrapper, test,
assembleDebug, dist/DollarLedger.apk + SHA256SUMS.txt, artifact DollarLedger-APK (30d).
Tag v* release workflow publishes personally installable debug APK and checksum.
Never commit keys/tokens. Explicitly document debug signing limitations.
Tests: conversion, rounding, balances, history, edits, deletes, cache, DAO, backups.
Observe actual Actions, repair failures until green, verify real APK artifact.
README: features, screens, architecture, privacy, API, backup, mobile download,
development/build instructions and known limitations. Do not label v1 complete
until all gates are met; report commit and actual CI/artifact status.
