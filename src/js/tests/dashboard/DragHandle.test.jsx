import React from 'react';

import { render } from '@testing-library/react';

import DragHandle from 'components/dashboard/DragHandle';

import '@testing-library/jest-dom';

/**
 * The dashboard icons were swapped from FontAwesome to react-icons as part of
 * the unified design. Because the bundle serves both states, each swap has to
 * choose at render time rather than at build time.
 *
 * DragHandle is the smallest of those components, so it stands in for the
 * pattern: what matters is that the FontAwesome markup is still emitted for
 * instances that have not opted in.
 */
describe('DragHandle icon gating', () => {
  afterEach(() => {
    delete window.UNIFIED_LAYOUT;
  });

  it('renders the FontAwesome icon when the instance has not opted in', () => {
    const { container } = render(<DragHandle />);

    expect(container.querySelector('i.fa.fa-ellipsis-v')).toBeInTheDocument();
    // react-icons renders an <svg>; there should be none in this state
    expect(container.querySelector('svg')).not.toBeInTheDocument();
  });

  it('renders the react-icons glyph when the instance has opted in', () => {
    window.UNIFIED_LAYOUT = true;

    const { container } = render(<DragHandle />);

    expect(container.querySelector('svg')).toBeInTheDocument();
    expect(container.querySelector('i.fa')).not.toBeInTheDocument();
  });

  it('keeps the wrapper markup identical in both states', () => {
    // Only the icon changes. The span and its class are what the sortable
    // handle binds to, so they must not differ between the two.
    const { container: off } = render(<DragHandle />);
    const offHandle = off.querySelector('.drag-handler');

    window.UNIFIED_LAYOUT = true;
    const { container: on } = render(<DragHandle />);
    const onHandle = on.querySelector('.drag-handler');

    expect(offHandle).toBeInTheDocument();
    expect(onHandle).toBeInTheDocument();
    expect(onHandle.tagName).toBe(offHandle.tagName);
    expect(onHandle.className).toBe(offHandle.className);
  });
});
