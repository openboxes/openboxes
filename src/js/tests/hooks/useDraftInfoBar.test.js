import { act, renderHook } from '@testing-library/react-hooks';
import { useDispatch, useSelector } from 'react-redux';
import { MemoryRouter, useHistory } from 'react-router-dom';

import * as actions from 'actions';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_COUNT_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import useDraftInfoBar from 'hooks/cycleCount/useDraftInfoBar';

jest.mock('react-redux', () => ({
  useDispatch: jest.fn(),
  useSelector: jest.fn(),
  connect: jest.fn(() => (Component) => Component),
}));
jest.mock('react-router-dom', () => ({
  useHistory: jest.fn(),
}));
jest.mock('selectors', () => ({
  getCurrentLocation: jest.fn(),
}));

describe('useDraftInfoBar', () => {
  const mockDispatch = jest.fn();
  const mockPush = jest.fn();
  const eraseDraftSpy = jest.spyOn(actions, 'eraseDraft');

  const renderUseDraftInfoBar = (tab) =>
    renderHook(() => useDraftInfoBar(tab), { wrapper: MemoryRouter });

  beforeEach(() => {
    jest.clearAllMocks();

    useDispatch.mockReturnValue(mockDispatch);
    useSelector.mockImplementation(() => ({ currentLocation: { id: 'loc-123' } }));
    useHistory.mockReturnValue({ push: mockPush });
  });

  it('returns discardDraft and continueDraft functions', () => {
    const { result } = renderUseDraftInfoBar(TO_RESOLVE_TAB);
    expect(typeof result.current.discardDraft).toBe('function');
    expect(typeof result.current.continueDraft).toBe('function');
  });

  it('dispatches eraseDraft with currentLocation.id and tab', () => {
    const { result } = renderUseDraftInfoBar(TO_RESOLVE_TAB);

    act(() => result.current.discardDraft());

    expect(eraseDraftSpy).toHaveBeenCalledWith('loc-123', TO_RESOLVE_TAB);
    expect(mockDispatch).toHaveBeenCalledWith(eraseDraftSpy.mock.results[0].value);
  });

  it('navigates correctly based on tab', () => {
    const { result: countTabResult } = renderUseDraftInfoBar(TO_COUNT_TAB);
    act(() => countTabResult.current.continueDraft());
    expect(mockPush).toHaveBeenCalledWith(CYCLE_COUNT.countStep());

    const { result: resoleTabResult } = renderUseDraftInfoBar(TO_RESOLVE_TAB);
    act(() => resoleTabResult.current.continueDraft());
    expect(mockPush).toHaveBeenCalledWith(CYCLE_COUNT.resolveStep());

    const { result: otherTabResult } = renderUseDraftInfoBar('RANDOM_TAB');
    act(() => otherTabResult.current.continueDraft());
    expect(mockPush).toHaveBeenCalledWith(CYCLE_COUNT.resolveStep());
  });

  it('handles missing currentLocation safely when discarding', () => {
    useSelector.mockImplementation(() => ({ currentLocation: undefined }));
    const { result } = renderUseDraftInfoBar('RANDOM_TAB');

    act(() => result.current.discardDraft());

    expect(eraseDraftSpy).toHaveBeenCalledWith(undefined, 'RANDOM_TAB');
    expect(mockDispatch).toHaveBeenCalledWith(eraseDraftSpy.mock.results[0].value);
  });
});
