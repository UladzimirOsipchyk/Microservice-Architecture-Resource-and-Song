**Contract Tests:**

✔ Song Service is the best candidate for contract tests because:
 - It exposes APIs that clients rely on.
 - It interacts with Resource Processor, making API stability crucial.
 - It can use message contracts if handling asynchronous events.

✔ Why Contract Tests Belong to the Controller Layer
 - They test the API contract
Contract tests validate how external clients (other microservices or frontend apps) interact with your API.

 - Since the controller layer exposes the API, contract tests focus on HTTP request/response validation.
 - They ensure API stability
If a controller changes its request/response format, contract tests will fail.
This prevents breaking changes for API consumers.
 - They verify serialization & deserialization
The controller layer handles JSON serialization (output) and deserialization (input).
Contract tests ensure the correct HTTP status codes, headers, and response body format.

