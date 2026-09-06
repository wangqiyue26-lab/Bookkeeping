# Dollar Ledger / 美元账本

Native Android personal bookkeeping with a professional navy-and-blue banking design. No login, advertising, bank connection, subscriptions or analytics.

- Application ID: `com.local.bookkeeping`
- Version: **1.0.0** (code 1)
- Android: **8.0+ / API 26**, compile and target SDK 36
- Repository: **wangqiyue26-lab/Bookkeeping**
- Source of truth: this repository; builds run entirely in GitHub Actions.

## Screens

**Home** — current total balance in USD, optional CNY underneath, saved-rate date, refresh, local account cards, income/expense actions and seven recent transactions.

**Account detail** — current CNY-derived USD balance and monthly transactions. Local visual IDs are not bank account numbers.

**Transactions** — date groups, month/type/account/category filters, details and editing.

**Statistics** — monthly historical USD income, expenses, net, category donut chart and bars.

**Settings** — network/manual exchange rate, theme, secondary CNY display, default account, account management, JSON backup, CSV and reset.

## Features

- Default Checking, Savings and Cash accounts start at zero; no sample transactions.
- Create, edit and archive accounts. Delete only accounts with zero opening balance and no transactions.
- Record income or expenses in CNY and immediately preview USD.
- Preserve each transaction's original exchange rate, rate date and USD amount.
- Edit transaction amount using its historical rate; choose **Use latest rate** explicitly to revalue it.
- Confirm transaction deletion, data reset and replacement import.
- Works offline using cached or manually entered rates.
- System, light and dark themes; original DL adaptive icon and Android SplashScreen.
- Storage Access Framework file pickers; no storage permissions.

## Financial rules

All account and transaction amounts are stored as integer cents. Rates are decimal strings. Conversion uses `BigDecimal`, two decimal places and `HALF_UP`.

```text
Current account CNY = opening CNY + income CNY - expense CNY
Current account USD = current account CNY × current rate
Total CNY = sum of current CNY balances of unarchived accounts
Total USD = total CNY × current rate
```

Historical USD amounts are never summed to derive current account balances.
Monthly statistics intentionally sum saved transaction USD amounts.

Example: CNY 1,000 at 0.139 saves USD 139.00 permanently. A new rate of 0.142 values the current CNY 1,000 balance at USD 142.00, while the historical transaction remains USD 139.00.

## Technology

Kotlin 2.2.21, Compose + Material 3 (BOM 2025.10.01), Room 2.8.4, KSP 2.2.21-2.0.4, DataStore, Retrofit, OkHttp, Kotlin Serialization, Coroutines/Flow, StateFlow, ViewModel and Navigation Compose. Gradle Kotlin DSL; AGP 8.13.2, Gradle 8.13, Temurin JDK 17.

## Architecture

- `domain/`: exact monetary calculations, validation and categories.
- `data/database/`: Room entities and DAO.
- `data/network/`: Retrofit exchange-rate endpoint.
- `data/repository/`: ledger operations, settings, cached rates and backup validation.
- `ui/`: ViewModel, design system, navigation and Compose screens.
- `LedgerApplication`: application-scoped dependencies.

Room Flow drives the interface. Database and file work runs off the main thread. Personal data never goes to an application server.

## Exchange Rate

[Frankfurter v2 documentation](https://frankfurter.dev/)

`GET https://api.frankfurter.dev/v2/rate/CNY/USD`

No API key. Frankfurter provides daily reference rates, not live trading quotes. The app stores rate, rate date and fetch time, and refreshes expired cache after six hours at startup. Existing data displays without waiting for a network response. Failures preserve saved rates.

On first installation without internet, open **Settings → Use manual rate**, enter e.g. `0.13920`, and select **Save manual rate**. You can then record transactions completely offline. Manual mode is never replaced automatically. Turning it off requests a fresh network rate.

## Privacy

Only the INTERNET permission is requested. No analytics, advertising, login, cloud synchronization, Firebase or sensitive device permissions. Android automatic backup is disabled. The only application network request is to Frankfurter and contains the currency pair, never your amounts, accounts or transactions. The API provider can see normal connection information such as your IP address.

JSON/CSV exports contain personal financial data. The system picker may offer a cloud file provider; you choose the destination. Exports are unencrypted.

## Build

You do **not** need a computer, Android Studio, Java or local Gradle.

The repository includes the complete official Gradle Wrapper (including its JAR). CI executes:

```sh
chmod +x gradlew
./gradlew --version
./gradlew test
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions

[Android Build](https://github.com/wangqiyue26-lab/Bookkeeping/actions/workflows/android-build.yml) runs on pushes to main, pull requests to main and manual dispatch. It tests, builds, packages `DollarLedger.apk`, computes `SHA256SUMS.txt`, and uploads **DollarLedger-APK** for 30 days.

**Android Release** tests and builds a personal debug APK for `v*` tags and manual runs. It also publishes the first versioned release after a successful main push build. Existing release assets are preserved. To release a new version, update versionName/versionCode, merge a green PR and use the matching tag.

## APK Download — Android 手机

### 方法 A：Actions Artifact

1. 手机浏览器登录 GitHub，打开本仓库。
2. **Actions → Android Build → 最新绿色运行**。
3. 滚动到 **Artifacts → DollarLedger-APK** 下载 ZIP。
4. 用手机文件管理器解压，点击 **DollarLedger.apk**。
5. 按 Android 提示允许该浏览器或文件管理器安装未知来源应用，再安装。

ZIP 里同时包含 **SHA256SUMS.txt**。不要下载仓库的源代码 ZIP。

手动构建：**Actions → Android Build → Run workflow → main → Run workflow**。

### 方法 B：Release 直接下载

1. 打开 [Releases](https://github.com/wangqiyue26-lab/Bookkeeping/releases)。
2. 选择最新版本，展开 **Assets**。
3. 下载 **DollarLedger-v1.0.0.apk**（后续版本对应 vX.X.X）。
4. 点击 APK 安装，不需要解压。

这是私有仓库，下载时需要登录有仓库访问权限的 GitHub 账户。

## Backup

**Export Backup** writes `DollarLedger_Backup_YYYY-MM-DD.json`, including schema version, accounts, transactions, settings and exchange-rate cache.

**Import Backup** reads and validates the entire file before showing replacement confirmation. It rejects unsupported schemas, duplicate IDs, orphan transactions, invalid amounts/rates/dates and inconsistent historical USD amounts. Database replacement is transactional. Limits: 20 MB, 10,000 accounts, 100,000 transactions.

**Export CSV** includes Date, Type, Account, Category, Amount CNY, Exchange Rate, Amount USD and Note. Amounts are positive; Type specifies direction. Cells are quoted and formula-leading text is escaped for spreadsheet safety.

**Delete All Data** deletes all accounts/transactions and recreates the three zero-balance defaults; display and exchange-rate preferences remain.

## Development

All changes belong in GitHub. Use `codex/develop` and a PR into `main`; inspect actual Actions logs and repair failing checks before merging. Detailed acceptance requirements live in [docs/ACCEPTANCE.md](docs/ACCEPTANCE.md).

Unit tests cover exact conversion/rounding, opening and running balances, historical values, cache age, validation, Room mutation/deletion, backup round-trip and invalid imports. A Robolectric activity smoke test checks startup without login or cached rates.

## Known Limitations

- This is a personal **debug-signed** build, not a Play Store release. Disposable GitHub runners may produce different debug signing keys. Always export a JSON backup before changing builds. If Android reports a signature mismatch, uninstalling erases local data; restore your exported backup after installing. Production signing should use GitHub Actions Secrets, never committed keys.
- Exchange rates are reference estimates and do not include bank spreads or fees.
- Currency is fixed to CNY → USD; account-to-account transfers are entered manually as separate records.
- UI currently uses English labels; Chinese app description is provided here.
- Large ledgers are held in memory for filtering/statistics; lists render lazily. Backup limits are enforced.
- Robolectric tests do not replace physical-device testing of installation, OEM file pickers and font scaling.

## License / Personal Use

Created for the repository owner's personal use. No affiliation with Bank of America or another bank. Original app design and DL icon; no bank logos or proprietary design assets. Third-party libraries retain their respective licenses. The official Gradle wrapper is distributed under Apache License 2.0.
