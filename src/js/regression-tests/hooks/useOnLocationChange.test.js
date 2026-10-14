import { renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import useOnLocationChange from 'hooks/useOnLocationChange';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
}));

describe('useOnLocationChange', () => {
  let currentLocation;

  beforeEach(() => {
    currentLocation = { id: 'loc-1', name: 'Main warehouse' };
    useSelector.mockImplementation((selector) => selector({ session: { currentLocation } }));
  });

  it('does not run the callback on the first render', () => {
    const callback = jest.fn();

    renderHook(() => useOnLocationChange(callback));

    expect(callback).not.toHaveBeenCalled();
  });

  it('runs the callback once per location change', () => {
    const callback = jest.fn();
    const { rerender } = renderHook(() => useOnLocationChange(callback));

    currentLocation = { id: 'loc-2', name: 'Depot' };
    rerender();
    rerender();
    expect(callback).toHaveBeenCalledTimes(1);

    currentLocation = { id: 'loc-1', name: 'Main warehouse' };
    rerender();
    expect(callback).toHaveBeenCalledTimes(2);
  });

  it('does not run the callback when the same location is loaded again', () => {
    const callback = jest.fn();
    const { rerender } = renderHook(() => useOnLocationChange(callback));

    currentLocation = { id: 'loc-1', name: 'Main warehouse (renamed)' };
    rerender();

    expect(callback).not.toHaveBeenCalled();
  });

  it('does not fail without a callback', () => {
    const { rerender } = renderHook(() => useOnLocationChange());

    currentLocation = { id: 'loc-2' };

    expect(() => rerender()).not.toThrow();
  });
});
