import { act, renderHook } from '@testing-library/react-hooks';

import useWindowWidthCheck from 'hooks/useWindowWidthCheck';

describe('useWindowWidthCheck', () => {
  const originalInnerWidth = window.innerWidth;

  const resizeWindowTo = (width) => act(() => {
    window.innerWidth = width;
    window.dispatchEvent(new Event('resize'));
  });

  beforeEach(() => {
    window.innerWidth = 1024;
  });

  afterAll(() => {
    window.innerWidth = originalInnerWidth;
  });

  it('tells whether the window is wider than the width', () => {
    expect(renderHook(() => useWindowWidthCheck(800)).result.current).toBe(true);
    expect(renderHook(() => useWindowWidthCheck(1024)).result.current).toBe(false);
    expect(renderHook(() => useWindowWidthCheck(1200)).result.current).toBe(false);
  });

  it('follows the window when it is resized', () => {
    const { result } = renderHook(() => useWindowWidthCheck(800));

    resizeWindowTo(700);
    expect(result.current).toBe(false);

    resizeWindowTo(900);
    expect(result.current).toBe(true);
  });

  it('checks again when the width changes', () => {
    const { result, rerender } = renderHook(({ width }) => useWindowWidthCheck(width), {
      initialProps: { width: 800 },
    });

    rerender({ width: 2000 });

    expect(result.current).toBe(false);
  });

  it('stops listening when unmounted', () => {
    const { result, unmount } = renderHook(() => useWindowWidthCheck(800));
    unmount();

    resizeWindowTo(700);

    expect(result.current).toBe(true);
  });
});
