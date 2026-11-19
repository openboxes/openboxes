import { act, renderHook } from '@testing-library/react';
import { useSelector } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';

import * as actions from 'actions';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_COUNT_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import useDraftInfoBar from 'hooks/cycleCount/useDraftInfoBar';

const mockDispatch = vi.fn();
const mockNavigate = vi.fn();
const eraseDraftSpy = vi.spyOn(actions, 'eraseDraft');

vi.mock('react-redux', () => ({
  useDispatch: vi.fn(() => mockDispatch),
  useSelector: vi.fn(() => ({ currentLocation: { id: 'loc-123' } })),
  connect: vi.fn(() => (Component) => Component),
}));
vi.mock('react-router-dom', async () => ({
  ...(await vi.importActual('react-router-dom')),
  useNavigate: vi.fn(() => mockNavigate),
}));
vi.mock('selectors', () => ({
  getCurrentLocation: vi.fn(),
}));

describe('useDraftInfoBar', () => {
  const renderUseDraftInfoBar = (tab) =>
    renderHook(() => useDraftInfoBar(tab), { wrapper: MemoryRouter });

  beforeEach(() => {
    vi.clearAllMocks();
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
    expect(mockNavigate).toHaveBeenCalledWith(CYCLE_COUNT.countStep());

    const { result: resoleTabResult } = renderUseDraftInfoBar(TO_RESOLVE_TAB);
    act(() => resoleTabResult.current.continueDraft());
    expect(mockNavigate).toHaveBeenCalledWith(CYCLE_COUNT.resolveStep());

    const { result: otherTabResult } = renderUseDraftInfoBar('RANDOM_TAB');
    act(() => otherTabResult.current.continueDraft());
    expect(mockNavigate).toHaveBeenCalledWith(CYCLE_COUNT.resolveStep());
  });

  it('handles missing currentLocation safely when discarding', () => {
    useSelector.mockImplementation(() => ({ currentLocation: undefined }));
    const { result } = renderUseDraftInfoBar('RANDOM_TAB');

    act(() => result.current.discardDraft());

    expect(eraseDraftSpy).toHaveBeenCalledWith(undefined, 'RANDOM_TAB');
    expect(mockDispatch).toHaveBeenCalledWith(eraseDraftSpy.mock.results[0].value);
  });
});
