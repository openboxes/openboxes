import { act, renderHook } from '@testing-library/react-hooks';

import useElementSize from 'hooks/useElementSize';

describe('useElementSize', () => {
  const resizableElement = (width, height) => {
    const element = new EventTarget();
    element.innerWidth = width;
    element.innerHeight = height;
    return element;
  };

  it('returns the size of the element', () => {
    const element = resizableElement(1280, 720);

    const { result } = renderHook(() => useElementSize(element));

    expect(result.current).toEqual({ width: 1280, height: 720 });
  });

  it('follows the element when it is resized', () => {
    const element = resizableElement(1280, 720);
    const { result } = renderHook(() => useElementSize(element));

    element.innerWidth = 640;
    element.innerHeight = 480;
    act(() => {
      element.dispatchEvent(new Event('resize'));
    });

    expect(result.current).toEqual({ width: 640, height: 480 });
  });

  it('stops listening when unmounted', () => {
    const element = resizableElement(1280, 720);
    jest.spyOn(element, 'removeEventListener');
    const { unmount } = renderHook(() => useElementSize(element));

    unmount();

    expect(element.removeEventListener).toHaveBeenCalledWith('resize', expect.any(Function));
  });
});
