# Environment matrix

| Environment | Frontend URL | Backend API URL | CORS status | Notes |
|---|---|---|---|---|
| Local dev (Next.js) | `http://localhost:3000` | `http://localhost:8080` | Allowed | Recommended default local FE |
| Local dev (Vite) | `http://localhost:5173` | `http://localhost:8080` | Allowed | Supported in current CORS config |
| Local dev (Angular) | `http://localhost:4200` | `http://localhost:8080` | Allowed | Supported in current CORS config |
| Staging | `TBD` | `TBD` | Not configured yet | Backend CORS whitelist update required |
| Production | `TBD` | `TBD` | Not configured yet | Backend CORS whitelist update required |

## Required frontend environment variables

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## Notes

- Current backend CORS whitelist only includes `http://localhost:3000`, `http://localhost:5173`, `http://localhost:4200`.
- Before staging/prod FE deployment, backend-side allowed origins must be added.
- FE should avoid hardcoding localhost URLs in runtime code.
- If FE wants custom trace propagation, send `X-Trace-Id` header; backend error responses echo or generate `traceId`.
