import { renderHook } from '@testing-library/react-hooks';
import { useDispatch } from 'react-redux';

import { HIDE_SPINNER, SHOW_SPINNER } from 'actions/types';
import useSpinner from 'hooks/useSpinner';

jest.mock('react-redux', () => ({
  useDispatch: jest.fn(),
  connect: jest.fn(() => (Component) => Component),
}));

describe('useSpinner', () => {
  const dispatch = jest.fn();

  beforeEach(() => {
    dispatch.mockClear();
    useDispatch.mockReturnValue(dispatch);
  });

  it('shows the spinner', () => {
    const { result } = renderHook(() => useSpinner());

    result.current.show();

    expect(dispatch).toHaveBeenCalledWith({ type: SHOW_SPINNER, payload: true });
  });

  it('hides the spinner', () => {
    const { result } = renderHook(() => useSpinner());

    result.current.hide();

    expect(dispatch).toHaveBeenCalledWith({ type: HIDE_SPINNER, payload: false });
  });
});
