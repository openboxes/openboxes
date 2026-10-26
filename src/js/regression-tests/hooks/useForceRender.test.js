import { act, renderHook } from '@testing-library/react-hooks';

import useForceRender from 'hooks/useForceRender';

describe('useForceRender', () => {
  it('renders the component again on every call', () => {
    let renders = 0;
    const { result } = renderHook(() => {
      renders += 1;
      return useForceRender();
    });
    expect(renders).toBe(1);

    act(() => result.current.forceRerender());
    act(() => result.current.forceRerender());

    expect(renders).toBe(3);
  });
});
