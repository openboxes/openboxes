import { renderHook } from '@testing-library/react-hooks';

import useScrollbar from 'hooks/useScrollbar';

describe('useScrollbar', () => {
  let element;

  beforeEach(() => {
    element = document.createElement('div');
    element.className = 'table-body';
    Object.defineProperty(element, 'scrollHeight', { value: 900 });
    document.body.appendChild(element);
  });

  afterEach(() => {
    element.remove();
  });

  it('scrolls the matched element to the bottom and back to the top', () => {
    const { result } = renderHook(() => useScrollbar({ selector: '.table-body' }));

    result.current.scrollToBottom();
    expect(element.scrollTop).toBe(900);

    result.current.scrollToTop();
    expect(element.scrollTop).toBe(0);
  });

  it('does nothing when no element matches', () => {
    const { result } = renderHook(() => useScrollbar({ selector: '.missing' }));

    expect(() => {
      result.current.scrollToBottom();
      result.current.scrollToTop();
    }).not.toThrow();
    expect(element.scrollTop).toBe(0);
  });
});
