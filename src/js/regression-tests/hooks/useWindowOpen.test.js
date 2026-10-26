import { renderHook } from '@testing-library/react-hooks';

import useWindowOpen from 'hooks/useWindowOpen';

describe('useWindowOpen', () => {
  beforeEach(() => {
    jest.spyOn(window, 'open').mockImplementation(() => null);
  });

  afterEach(() => {
    window.open.mockRestore();
  });

  it('opens the url in a new tab by default', () => {
    const { result } = renderHook(() => useWindowOpen());

    result.current.openWindow('/openboxes/order/print/1');

    expect(window.open).toHaveBeenCalledWith('/openboxes/order/print/1', '_blank');
  });

  it('opens the url in the given target', () => {
    const { result } = renderHook(() => useWindowOpen());

    result.current.openWindow('/openboxes/dashboard', '_self');

    expect(window.open).toHaveBeenCalledWith('/openboxes/dashboard', '_self');
  });

  it.each([undefined, null, ''])('opens nothing for the url %p', (url) => {
    const { result } = renderHook(() => useWindowOpen());

    result.current.openWindow(url);

    expect(window.open).not.toHaveBeenCalled();
  });
});
