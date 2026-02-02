# Consultation API

This is a small REST API for the "Consultation" phase of the user journey. It:

- Serves questions to the frontend.
- Receives answers to those questions.
- Returns a JSON indicating whether we are likely to be able to prescribe medication.

No permanent storage is used; all data is held in memory.

---

## Tech stack

- Java 17
- Spring Boot 3
- Gradle
- JUnit 5 + AssertJ
- Mockito for unit tests
- WebFlux‑style controller tests

---

## High‑level design

### Key components

- `ConsultationController` – REST endpoints:
    - `GET /api/consultation/questions`
    - `POST /api/consultation/answers?sessionId=...`
    - `GET /api/consultation/decision?sessionId=...`
- `ConsultationService` – orchestrates:
    - Question retrieval.
    - Answer storage (in‑memory by session).
    - Prescription‑decision logic.
- `PrescriptionRules` – encapsulates business rules:
    - Must answer all questions.
    - No known allergies.
    - No current medication.
    - Invalid answers (`"yes"`/`"no"`) are rejected.
- `InMemoryConsultationSessionRepository` – stores `ConsultationSession` per `sessionId`.
- `ConsultationQuestions` – holds the in‑memory question list; can be replaced later with DB/file/HTTP.

### Decision logic

- A session must answer **all three questions**.
- If any answer is not `"yes"` or `"no"` for yes/no questions, the decision is `likelyToPrescribe = false` with a reason.
- If the user has **known allergies** or is **taking medication**, the decision is `false`.
- Otherwise, the decision is `true`.

Example JSON response:

```json
{
  "likelyToPrescribe": false,
  "reasons": [
    "Patient reports known allergies; prescribing is not safe."
  ]
}
```

## Trade‑offs

- In‑memory storage only: sessions are held in a `HashMap`; this is fine for the exercise but would be replaced with a database or Redis in production.
- Simple business logic: the “likely to prescribe” decision is based on a small set of yes/no rules; real logic would be medically validated and more complex.
- No auth or rate‑limiting: omitted for simplicity.
- Error handling: missing or invalid sessions return `400 Bad Request` with a clear message.

---

## How to run locally

1. Ensure Java 17 is installed.
2. Clone the repo:
   ```bash
   git clone <your-repo-url>
   ```
3. Build and run:   
   ```bash
   cd medex-consultation
   ./gradlew bootRun
   ``` 
   The API will start on http://localhost:8080.

## How to exercise the API
1. Get questions
```bash
    curl -s http://localhost:8080/api/consultation/questions | jq
 ```
```json
[
  {
    "id": "q1",
    "text": "Do you have any known allergies?",
    "type": "yesno"
  },
  {
    "id": "q2",
    "text": "Are you currently taking any medication?",
    "type": "yesno"
  },
  {
    "id": "q3",
    "text": "Describe your symptoms briefly.",
    "type": "text"
  }
]
```
2. Submit answers
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '[
    { "questionId": "q1", "value": "no" },
    { "questionId": "q2", "value": "no" },
    { "questionId": "q3", "value": "headache" }
  ]' \
  http://localhost:8080/api/consultation/answers?sessionId=s1 
 ```

3. Get prescription decision
```bash
   curl -s http://localhost:8080/api/consultation/decision?sessionId=s1 | jq
 ```
If all answers are valid and low‑risk:
```json
  {
    "likelyToPrescribe": true,
    "reasons": []
  }
```
If the session is not found:
``` text
HTTP 400 Bad Request
```
If an answer is invalid (not "yes"/"no"):
```json
{
  "likelyToPrescribe": false,
  "reasons": [
    "Answer to allergy question is invalid (must be 'yes' or 'no')."
  ]
}
```

## Tests
The project follows a TDD‑style approach

To run all tests:
   ```bash
   ./gradlew test
   ```

## Notes on evolution

- The in‑memory question list in `ConsultationQuestions` can be replaced with:
    - A JSON file.
    - A database table.
    - An external HTTP API.
- The rules in `PrescriptionRules` can be extended with:
    - Risk scores.
    - More questions.
    - External validation services.
- In production, you would add:
    - A global controller could be added by @ControllerAdvice for more insightful api responses. 
    - Auth / JWT.
    - Rate limiting.
    - Persistent storage (e.g. PostgreSQL or Redis).
    - Logging and monitoring.





