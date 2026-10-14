import { renderHook } from '@testing-library/react-hooks';

import { SET_SCROLL_TO_BOTTOM } from 'actions/types';
import useScrollToBottom from 'hooks/outboundImport/useScrollToBottom';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));

describe('useScrollToBottom', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('scrolls the attached button into view and resets the flag', () => {
    mockState = { outboundImport: { scrollToBottom: false } };
    const scrollIntoView = jest.fn();
    const { result, rerender } = renderHook(() => useScrollToBottom());
    result.current.nextButtonRef.current = { scrollIntoView };

    mockState = { outboundImport: { scrollToBottom: true } };
    rerender();

    expect(scrollIntoView).toHaveBeenCalledTimes(1);
    expect(mockDispatch).toHaveBeenCalledWith({ type: SET_SCROLL_TO_BOTTOM, payload: false });
  });

  it('does nothing while no button is attached', () => {
    mockState = { outboundImport: { scrollToBottom: true } };

    renderHook(() => useScrollToBottom());

    expect(mockDispatch).not.toHaveBeenCalled();
  });
});
