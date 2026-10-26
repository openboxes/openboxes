import { renderHook } from '@testing-library/react-hooks';

import useSessionStorage from 'hooks/useSessionStorage';

describe('useSessionStorage', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('returns the default value when nothing is stored', () => {
    const { result } = renderHook(() => useSessionStorage('filters', { page: 1 }));

    expect(result.current[0]).toEqual({ page: 1 });
  });

  it('returns the stored value, parsed from JSON', () => {
    sessionStorage.setItem('filters', JSON.stringify({ page: 3, status: ['PENDING'] }));

    const { result } = renderHook(() => useSessionStorage('filters', { page: 1 }));

    expect(result.current[0]).toEqual({ page: 3, status: ['PENDING'] });
  });

  it('keeps a stored falsy value instead of the default', () => {
    sessionStorage.setItem('collapsed', 'false');

    const { result } = renderHook(() => useSessionStorage('collapsed', true));

    expect(result.current[0]).toBe(false);
  });

  it('stores a value as JSON and clears it', () => {
    const { result, rerender } = renderHook(() => useSessionStorage('tab', 'all'));

    result.current[1]({ name: 'pending' });
    expect(sessionStorage.getItem('tab')).toBe('{"name":"pending"}');
    rerender();
    expect(result.current[0]).toEqual({ name: 'pending' });

    result.current[2]();
    expect(sessionStorage.getItem('tab')).toBeNull();
    rerender();
    expect(result.current[0]).toBe('all');
  });
});
