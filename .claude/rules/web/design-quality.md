---
paths:
  - "src/js/**"
  - "grails-app/views/**"
---

# UI Quality — OpenBoxes Frontend

OpenBoxes is a **data-heavy logistics admin app**, not a marketing site. The visual priorities are **density, scannability, and predictability** — not hero sections, scrollytelling, or editorial layouts. Rules here target the admin-app reality.

## Stack Constraints

- **Bootstrap 4.6** + SCSS is the foundation. Use Bootstrap utilities and components first; custom CSS is a fallback.
- **Font Awesome 5** icons via `@fortawesome/react-fontawesome`. Don't mix icon libraries.
- **react-tippy** for tooltips, **react-modal** for modals, **react-confirm-alert** for confirmations — stick with what's in use.
- **`react-table` / `react-virtualized` / `@tanstack/react-table`** are all present — use the one already used by the surrounding screen rather than introducing a new one.

## Hierarchy and Density

- Forms should group fields by logical section with clear headings. Wizard steps (`src/js/components/stock-movement-wizard/**`) are the template to follow.
- Tables should expose the most-scanned column first (usually product code/SKU), with secondary data right-aligned (qty, value).
- **Don't pad aggressively.** This is an admin app; compact layouts let users scan 50+ rows without scrolling.

## Consistency Before Creativity

- New screens should match the visual language of existing screens in the same area. Inbound-list, outbound-list, stock-list all follow a list-page pattern (`src/js/hooks/list-pages/**`) — copy it.
- When adding a new wizard step, match the existing wizard step layout (header, breadcrumbs, save/cancel row).
- Modals should use the existing `ModalWrapper` (`src/js/components/form-elements/ModalWrapper.jsx`) pattern.

## States to Always Design

Every interactive element needs explicit visual states:

- **Default, hover, focus, active, disabled, loading**
- **Empty state** (list with no data) — use an informative empty state, not a blank area
- **Error state** (API failure) — use the existing `AlertMessage` pattern
- **Loading state** — use `react-spinners` or skeleton placeholders; never leave a component with stale data

## Accessibility Minimums

- Use semantic HTML: `<button>` for actions, `<a>` for navigation, `<table>` / `<th>` / `<td>` for tabular data.
- Every icon-only button needs an `aria-label` or `title`.
- Focus order must be logical; test keyboard navigation on any new form or wizard step.
- Form labels must be associated with inputs (`<label htmlFor>` or wrapping).

## Internationalization

- **All user-facing strings must go through `react-localize-redux`.** No literal English strings in JSX. New keys go in the translations JSON and the default strings.
- Format dates with `date-fns` (v4), numbers with `Intl.NumberFormat`. Don't hardcode formats.
- Currency, units, and formats are locale-dependent — respect the user's locale.

## Anti-Patterns for This App

- **Don't reach for glassmorphism, neo-brutalism, or bento layouts.** They don't fit a data-entry tool and will look out of place next to existing screens.
- **Don't introduce motion that delays interaction.** Animations longer than ~200ms feel slow in a tool users spend 8 hours in.
- **Don't auto-scroll or animate on data refresh.** Users lose their place.
- **Don't dark-mode-by-default.** The app has one theme; don't invent a second one without coordination.

## Checklist

- [ ] Matches the visual language of adjacent screens
- [ ] Uses the same table/form/modal components as its neighbors
- [ ] Defines all interactive states (including empty, error, loading)
- [ ] Passes keyboard navigation test
- [ ] All strings go through `react-localize-redux`
- [ ] Semantic HTML (`<button>`, `<table>`, `<label for>`)
- [ ] No new design tokens or color variables without justification
