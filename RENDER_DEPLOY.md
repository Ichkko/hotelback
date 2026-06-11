# Render deploy

## Dashboard settings

Use **New > Web Service** and connect:

- Repository: `https://github.com/Ichkko/hotelback`
- Branch: `main`
- Root Directory: leave empty
- Language: `Docker`
- Instance Type: `Free` or your preferred paid plan

Render will use the repository `Dockerfile`.

## Required environment variables

Set these in the Render service **Environment** tab before deploying:

```text
DB_URL=jdbc:mysql://YOUR_HOST:3306/YOUR_DATABASE?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=YOUR_DATABASE_USER
DB_PASSWORD=YOUR_DATABASE_PASSWORD
JWT_TOKEN=at-least-32-characters-secret-value
```

Optional production values:

```text
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
QPAY_BASE_URL=https://merchant.qpay.mn
QPAY_USERNAME=...
QPAY_PASSWORD=...
QPAY_INVOICE_CODE=...
QPAY_CALLBACK_URL=https://YOUR_RENDER_URL/api/payments/qpay/callback
QPAY_SENDER_BRANCH_CODE=ONLINE
QPAY_SENDER_STAFF_CODE=online
```

## Notes

- This app is configured for MySQL. Use an external MySQL database and allow Render outbound connections.
- Render provides the `PORT` environment variable automatically; `application.properties` reads it.
- Production runs with the `prod` Spring profile from the Dockerfile.
