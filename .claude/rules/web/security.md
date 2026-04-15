---
paths:
  - "src/js/**"
  - "grails-app/views/**"
  - "grails-app/assets/**"
---

# Web Security — OpenBoxes Frontend

## XSS Prevention

- **Never use `dangerouslySetInnerHTML`** unless content is sanitized with a vetted library (DOMPurify) first. Even then, prefer rendering JSX.
- **`react-html-parser`** is in use in this repo — treat its input as untrusted. Always sanitize user-supplied HTML before parsing.
- **GSP views** (`grails-app/views/**/*.gsp`): use `${...}` which escapes by default. Never use `<%= %>` or `raw()` / `.encodeAsRaw()` with user input.
- **Axios responses**: don't inject raw response HTML into the DOM. If the backend returns HTML, render it as text or sanitize.

## Input Validation

- Validate on **both** client and server. Client-side validation is for UX; never trust it for security.
- Use `zod` schemas for new forms (already a dependency). Existing forms use `react-final-form` validators — keep the pattern consistent within a given wizard.
- Reject input at the boundary with a clear error; never silently coerce.

## API Calls

- **Always use the shared `apiClient`** (`src/js/utils/apiClient.jsx`) — never `fetch` or a bare `axios` instance. The shared client handles auth, CSRF, and error interceptors.
- **CSRF:** the backend issues a CSRF token; the shared `apiClient` attaches it. If you're tempted to bypass the shared client, don't — you'll lose CSRF protection.
- **Never put secrets in `src/js/`** — anything shipped to the browser is public. Use `process.env.*` only for build-time, non-sensitive config.

## Sensitive Data

- **No PII in console logs.** `console.log` left in production code is both a security and quality issue. Use the existing Sentry integration (`@sentry/react`) for error reporting.
- **No secrets in Redux state** — Redux is persisted via `redux-persist` into localStorage. Tokens, passwords, API keys must never go through Redux.
- **`redux-persist` whitelists**: when adding a new reducer, review whether its state should be persisted. Default to **not** persisting user-specific or sensitive state.

## File Uploads

- Validate MIME type and size on the client for UX, and **always** validate again server-side.
- `react-dropzone` is the standard — use its `accept` and `maxSize` props.
- Never display a server path or internal filename to users in error messages.

## Third-Party Scripts

- New runtime dependencies must be justified in the PR description. Prefer existing utilities over adding new packages — every dep is a supply-chain surface.
- Respect the Node 14 / React 16.8 version floors when evaluating packages; many modern packages won't install or will ship unsupported syntax.

## URLs and Redirects

- Never redirect to a URL pulled from `query-string` params without validating it against an allowlist. Open redirects are a common phishing vector in admin apps.
- `react-router-dom` v5 is in use — use `<Redirect>` and `history.push()`, not `window.location = ...`.

## Logging and Error Reporting

- Use Sentry for client errors; redact user PII (names, emails, addresses) from breadcrumbs via Sentry's `beforeSend`.
- Never log request bodies or response bodies that might contain inventory-sensitive data (lot numbers, quantities, supplier prices).

## Checklist

- [ ] No `dangerouslySetInnerHTML` with unsanitized input
- [ ] API calls go through `apiClient`, not bare axios/fetch
- [ ] No secrets in source, Redux, or localStorage
- [ ] Forms validate on both client and server
- [ ] Redirects validated against allowlist
- [ ] No `console.log` with user data
