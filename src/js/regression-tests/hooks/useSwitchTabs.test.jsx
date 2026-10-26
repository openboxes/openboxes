import React from 'react';

import { act, renderHook } from '@testing-library/react-hooks';
import { createMemoryHistory } from 'history';
import { Router } from 'react-router-dom';

import useSwitchTabs from 'hooks/useSwitchTabs';

describe('useSwitchTabs', () => {
  const renderAt = (url) => {
    const history = createMemoryHistory({ initialEntries: [url] });
    const wrapper = ({ children }) => <Router history={history}>{children}</Router>;
    return { history, ...renderHook(() => useSwitchTabs({ defaultTab: 'ALL' }), { wrapper }) };
  };

  it('opens the default tab when the url names none, keeping the other parameters', () => {
    const { history } = renderAt('/openboxes/cycleCount?max=10');

    expect(history.location.pathname).toBe('/openboxes/cycleCount');
    expect(history.location.search).toBe('?max=10&tab=ALL');
    expect(history.length).toBe(2);
  });

  it('stays on the tab the url names', () => {
    const { history } = renderAt('/openboxes/cycleCount?tab=TO_COUNT');

    expect(history.location.search).toBe('?tab=TO_COUNT');
    expect(history.length).toBe(1);
  });

  it('switches the tab and runs the callback', () => {
    const { history, result } = renderAt('/openboxes/cycleCount?tab=TO_COUNT&max=10');
    const onSwitchTab = jest.fn();

    act(() => result.current.switchTab('TO_RESOLVE', onSwitchTab));

    expect(history.location.pathname).toBe('/openboxes/cycleCount');
    expect(history.location.search).toBe('?max=10&tab=TO_RESOLVE');
    expect(onSwitchTab).toHaveBeenCalledTimes(1);
  });

  it('switches the tab without a callback', () => {
    const { history, result } = renderAt('/openboxes/cycleCount?tab=TO_COUNT');

    act(() => result.current.switchTab('ALL'));

    expect(history.location.search).toBe('?tab=ALL');
  });
});
