# Link shortener

Run `mvn spring-boot:run`. The default public address is `http://localhost:7070`;
set `APP_BASE_URL` to the public origin when deploying. Keep the H2 `data/`
directory on persistent storage. Run the integration tests with `mvn test`.

| Request | Result |
| --- | --- |
| POST /api/v1/urls | Creates a short link (201). Save the response header `X-Management-Token`; it is returned only once. |
| GET /{code} | Redirects (302) and increments the click count. An expired link returns 410. |
| GET /api/v1/urls/{code} | Returns the destination, expiration and click count without counting a visit. |
| DELETE /api/v1/urls/{code} | Requires the creation token in `X-Management-Token`. Returns 204 on success, 403 for an invalid token, or 404 for an unknown code. |

Example creation body: `{"originalUrl":"https://example.org/guide","alias":"guide"}`.
This produces `/@guide`. Omit `alias` for an automatically generated code.
Aliases accept 3–40 lowercase letters, digits and hyphens, starting with a letter or digit.
An optional `expirationDate` accepts a future ISO timestamp with a timezone.
Duplicate destinations or aliases return 409. Destinations must be absolute HTTP(S)
URLs without embedded credentials and can contain up to 2048 characters.

Links created before management tokens were introduced remain readable. They cannot
be deleted through the public API; an administrator must manage those records.
Before upgrading an existing H2 database, back it up and resolve any duplicate
original URLs so the unique constraint can be applied.

Anonymous creation and abuse reports share a quota of 30 POST attempts per
client address per calendar minute. Set `WRITES_PER_MINUTE` to change it.
A rejected request returns 429 and a `Retry-After` duration in seconds.
The quota is local to one process, resets on restart, and uses the servlet
remote address. Behind a proxy, configure quotas at the trusted edge; otherwise
clients behind that proxy can share one allowance.

Report a link with `POST /api/v1/urls/{code}/reports`. Multiple reports create
one pending item. Reporting does not disable the link. Configure a random
`MODERATOR_TOKEN` in the server environment and use HTTPS. With the token in
the `X-Moderator-Token` header, list pending codes using
`GET /api/v1/moderation/reports?page=0` (50 per page). Review with
`PUT /api/v1/moderation/links/{code}?disabled=true`; use `false` to dismiss
or restore. Disabled redirects return 410 without recording clicks. With no
configured token, moderator operations are unavailable.
