import { act, renderHook } from '@testing-library/react-hooks';

import useHandleModalAction from 'hooks/useHandleModalAction';

describe('useHandleModalAction', () => {
  it('starts closed', () => {
    const { result } = renderHook(() => useHandleModalAction());

    expect(result.current).toMatchObject({ isOpen: false, data: null, type: null });
  });

  it.each([true, false])('opens the modal and resolves with the answer %p', async (answer) => {
    const { result } = renderHook(() => useHandleModalAction());
    const rows = [{ id: 'r1' }];

    let answered;
    act(() => {
      answered = result.current.openModal({ data: rows, type: 'EXPIRY' });
    });
    expect(result.current).toMatchObject({ isOpen: true, data: rows, type: 'EXPIRY' });

    act(() => result.current.handleResponse(answer));

    await expect(answered).resolves.toBe(answer);
    expect(result.current).toMatchObject({ isOpen: false, data: null, type: null });
  });

  it('closes without failing when no modal was opened', () => {
    const { result } = renderHook(() => useHandleModalAction());

    expect(() => act(() => result.current.handleResponse(true))).not.toThrow();
    expect(result.current.isOpen).toBe(false);
  });
});
