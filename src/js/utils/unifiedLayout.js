/**
 * Whether this instance has opted in to the unified design.
 *
 * Set on window by layouts/react.gsp from
 * openboxes.unifiedLayout.enabled. The React bundle is a single build
 * serving both states, so components that differ between them read this
 * rather than being compiled twice.
 */
// eslint-disable-next-line import/prefer-default-export
export const isUnifiedLayout = () =>
  typeof window !== 'undefined' && window.UNIFIED_LAYOUT === true;
