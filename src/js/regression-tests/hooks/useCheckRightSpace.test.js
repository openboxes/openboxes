import { act, renderHook } from '@testing-library/react-hooks';

import useCheckRightSpace from 'hooks/useCheckRightSpace';

describe('useCheckRightSpace', () => {
  const originalInnerWidth = window.innerWidth;

  // An element whose right edge is at `right`, `width` pixels wide
  const elementAt = (right, width) => ({
    getBoundingClientRect: () => ({ right, width }),
  });

  beforeEach(() => {
    window.innerWidth = 1000;
  });

  afterAll(() => {
    window.innerWidth = originalInnerWidth;
  });

  it('aligns left while the element fits once more to its right', () => {
    const { result } = renderHook(() => useCheckRightSpace());
    result.current.elementReference.current = elementAt(600, 300);
    act(() => result.current.setIsVisible(true));

    expect(result.current.shouldAlignLeft()).toBe(true);
  });

  it('does not align left when the element would overflow the window', () => {
    const { result } = renderHook(() => useCheckRightSpace());
    result.current.elementReference.current = elementAt(700, 300);
    act(() => result.current.setIsVisible(true));

    expect(result.current.shouldAlignLeft()).toBe(false);
  });

  it('does not align left while the element is hidden or not mounted', () => {
    const { result } = renderHook(() => useCheckRightSpace());
    expect(result.current.shouldAlignLeft()).toBe(false);

    result.current.elementReference.current = elementAt(100, 100);
    expect(result.current.shouldAlignLeft()).toBe(false);
  });
});
