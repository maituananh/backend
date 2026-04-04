# Pawn Service Backend

This project contains the backend service for the Pawn-service website, along with integrated infrastructure management guidelines (Terraform).

## 🔗 Related Repositories
For the system to be fully functional, you should refer to the following repositories:
- **Backend (this repo):** [https://github.com/maituananh/backend](https://github.com/maituananh/backend)
- **Frontend (FE):** [https://github.com/maituananh/pawn-service-fe](https://github.com/maituananh/pawn-service-fe)
- **CI/CD Configuration:** [https://github.com/maituananh/pawn-service-cicd](https://github.com/maituananh/pawn-service-cicd)
- **Terraform Configuration:** [https://github.com/maituananh/pawn-service-terraform](https://github.com/maituananh/pawn-service-terraform)

## 🛠 Technology Stack
**Backend:**
- Java 21
- Spring Boot
- MySQL

**Frontend:**
- ReactJS

---

## 🚀 Quick Start
Start services quickly using Docker Compose:
```bash
docker-compose up -d
```

### Swagger Documentation
- **UI:** http://localhost:8080/swagger-ui/index.html
- **API Token Generation:** `/api/auth/token` (default credentials: `admin` / `admin`)

---

## 💻 Installation & Local Development

### 1. Setup Backend
1. Clone the project:
   ```bash
   git clone git@github.com:maituananh/backend.git
   cd backend
   ```
2. Build and start the server:
   ```bash
   ./gradlew build
   ./gradlew bootRun
   ```

### 2. Default Test Accounts
- **ADMIN:** admin / admin
- **CUSTOMER:** user / user

### 3. API Examples
**Login:**
```bash
curl --location --request POST 'http://localhost:8080/api/auth/token' \
--header 'Content-Type: application/json' \
--data '{
    "username": "admin",
    "password": "admin"
}'
```

**Product Creation:**
```bash
curl --location --request POST 'http://localhost:8080/api/products' \
--header 'Content-Type: application/json' \
--header 'Authorization: <your-access-token>' \
--data '{
    "name": "product1",
    "price": 12.2,
    "startDay": "2025-08-09T00:00:00Z",
    "endDate": "2025-08-09T00:00:00Z",
    "type": "abc"
}'
```

---

## 💳 Stripe Webhook Local Setup
To correctly process payments on your local environment without exposing your server to the internet, you need to use the Stripe CLI to redirect webhook events.

### Step-by-step Setup:
1. **Install Stripe CLI** and log in via terminal:
   ```bash
   stripe login --api-key <YOUR_STRIPE_SECRET_KEY>
   ```

2. **Start the Webhook Listener:**
   This command forwards Stripe events directly to your local Spring Boot server's webhook endpoint:
   ```bash
   stripe listen --events checkout.session.completed,checkout.session.expired,payment_intent.payment_failed --forward-to localhost:8080/api/payment/webhook
   ```
   *(Note: There is also a helper script at `stripe/webhook.sh` you can use)*
   
   ⚠️ **IMPORTANT:** Once started, the CLI will output a webhook signing secret looking like `whsec_...`. **Copy this secret for the next step**.

3. **Configure `application.yml`:**
   Take the webhook secret from the previous step and provide it as `STRIPE_WEBHOOK_SECRET` in `src/main/resources/application.yml` (or export it as an ENV variable):
   ```yaml
   stripe:
     secret-key: ${STRIPE_API_KEY:sk_test_...}
     webhook-secret: ${STRIPE_WEBHOOK_SECRET:whsec_...} # OVERWRITE THIS with the key from 'stripe listen'
     success-url: ${STRIPE_SUCCESS_URL:${frontend.base-url}/payment-success}
     cancel-url: ${STRIPE_CANCEL_URL:${frontend.base-url}/mycart}
   ```

4. **Trigger a Test Event (Optional):**
   Open a new terminal window and trigger a completed checkout event to test your backend handling:
   ```bash
   stripe trigger checkout.session.completed
   ```

---

## ⚙️ Environment Variables
Besides Stripe, configure the following variables in `application.yml` or set them as system variables:

**Database / Storage / Caching:**
- `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `AWS_S3_ACCESS_KEY`, `AWS_S3_SECRET_KEY`, `AWS_S3_ENDPOINT`

**Other Services:**
- `AI_OPENAI_API_KEY`: API Key for ChatGPT processing features.
- `JOBRUNR_USERNAME`, `JOBRUNR_PASSWORD`: Dashboard credentials for background jobs.
- `BE_PORT` (default 8080), `FRONTEND_BASE_URL` (default http://localhost:5173).

---



## 🧪 Testing Coverage
To run unit and integration tests:
```bash
./gradlew test
```
