# SkinMe Route Reference Guide

## Public Routes (No Authentication Required)

### Page Routes

| Route                 | Template              | Description                     |
| --------------------- | --------------------- | ------------------------------- |
| `GET /`               | `index.html`          | Homepage with products showcase |
| `GET /login-page`     | `login.html`          | Admin login page                |
| `GET /signup`         | `signup.html`         | User registration page          |
| `GET /reset-password` | `reset-password.html` | Password recovery page          |

### API Routes

| Route                         | Description             |
| ----------------------------- | ----------------------- |
| `GET /api/v1/products/all`    | All products            |
| `GET /api/v1/categories/all`  | All categories          |
| `GET /api/v1/chat/assistant`  | Chat with AI assistant  |
| `POST /api/v1/chat/assistant` | Send chat message to AI |

## Protected Routes (Authentication Required)

### Page Routes

| Route            | Template         | Description     |
| ---------------- | ---------------- | --------------- |
| `GET /dashboard` | `dashboard.html` | Admin dashboard |

### Admin API Routes

| Route                                  | Method | Description     |
| -------------------------------------- | ------ | --------------- |
| `POST /api/v1/admin/products`          | POST   | Create product  |
| `PUT /api/v1/admin/products/{id}`      | PUT    | Update product  |
| `DELETE /api/v1/admin/products/{id}`   | DELETE | Delete product  |
| `POST /api/v1/admin/categories`        | POST   | Create category |
| `PUT /api/v1/admin/categories/{id}`    | PUT    | Update category |
| `DELETE /api/v1/admin/categories/{id}` | DELETE | Delete category |

## Authentication Routes

| Route                        | Method | Description       |
| ---------------------------- | ------ | ----------------- |
| `POST /api/v1/auth/register` | POST   | Register new user |
| `POST /api/v1/auth/login`    | POST   | Login user        |
| `POST /logout`               | POST   | Logout user       |
| `POST /api/v1/auth/refresh`  | POST   | Refresh JWT token |

## WebSocket Routes

| Route                  | Description                     |
| ---------------------- | ------------------------------- |
| `WS /ws-endpoint`      | Main WebSocket endpoint (STOMP) |
| `/app/chat/message`    | Send chat message               |
| `/app/chat/query`      | Send AI query                   |
| `/topic/chat`          | Subscribe to chat messages      |
| `/topic/notifications` | Subscribe to notifications      |
| `/topic/orders`        | Subscribe to order updates      |
| `/topic/products`      | Subscribe to product updates    |
| `/topic/inventory`     | Subscribe to inventory updates  |

## Special Routes

| Route                 | Template              | Description            |
| --------------------- | --------------------- | ---------------------- |
| `GET /websocket-demo` | `websocket-demo.html` | WebSocket testing page |

## Redirect Routes

| From             | To                       | Condition           |
| ---------------- | ------------------------ | ------------------- |
| `/login` (POST)  | `/dashboard`             | Successful login    |
| `/logout` (POST) | `/login-page?logout`     | Logout              |
| `/products/**`   | `/api/v1/products/all`   | Product management  |
| `/categories/**` | `/api/v1/categories/all` | Category management |

## Quick Navigation Links

### From Homepage

```
Home (/)
├── Products → /api/v1/products/all
├── Chat AI → /api/v1/chat/assistant
└── Login → /login-page
```

### From Login Page

```
Login Page (/login-page)
├── Login (POST) → /dashboard
├── Create Account → /signup
└── Reset Password → /reset-password
```

### From Dashboard

```
Dashboard (/dashboard)
├── Products → /api/v1/products/all
├── Categories → /api/v1/categories/all
├── Orders → /api/v1/orders/all
├── Users → /api/v1/users/all
├── Chat & Notifications → /websocket-demo
└── Logout → /logout
```

## Session Management

- **Session Creation**: Automatic on login
- **Session Validation**: JWT token-based
- **Session Duration**: Configured in SecurityConfig
- **CSRF Protection**: Enabled on all forms
- **Remember-Me**: Optional (can be enabled in login form)

## Error Handling

| HTTP Code | Response              | Handling                      |
| --------- | --------------------- | ----------------------------- |
| 401       | Unauthorized          | Redirect to login page        |
| 403       | Forbidden             | Display access denied message |
| 404       | Not Found             | Show 404 error page           |
| 500       | Internal Server Error | Show error page               |

## Cookie & Header Configuration

### CORS Settings

- **Allowed Origins**:
  - http://localhost:5173
  - http://localhost:8800
  - https://skinme.store
  - https://www.skinme.store
  - https://backend.skinme.store

### CORS Methods

- GET, POST, PUT, DELETE, OPTIONS

### CORS Headers

- Authorization
- Content-Type
- Accept

## Testing Routes

### Via cURL

```bash
# Login
curl -X POST http://localhost:8800/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin@example.com&password=password"

# Get all products
curl http://localhost:8800/api/v1/products/all

# Chat with AI
curl -X POST http://localhost:8800/api/v1/chat/assistant \
  -H "Content-Type: application/json" \
  -d '{"content": "What cleanser do you recommend?"}'
```

## Notes

- All timestamps are in UTC
- All responses are in JSON format (except HTML pages)
- API prefix is `/api/v1` (configurable in properties)
- WebSocket uses STOMP protocol with SockJS fallback
- Session timeout: 30 minutes (default)
