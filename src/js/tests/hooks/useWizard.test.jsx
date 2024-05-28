import React from 'react';

import { act, renderHook } from '@testing-library/react-hooks';

import useWizard from 'hooks/useWizard';

describe('Changing steps', () => {
  const steps = [
    {
      key: 1,
      Component: () => (<div>First compoment</div>),
      title: 'First',
    },
    {
      key: 2,
      Component: () => (<div>Second compoment</div>),
      title: 'Second',
    },
    {
      key: 3,
      Component: () => (<div>Third compoment</div>),
      title: 'Third',
    },
    {
      key: 4,
      Component: () => (<div>Fourth compoment</div>),
      title: 'Fourth',
    },
  ];

  it('wizard will start with the initial key', () => {
    const { result } = renderHook(() => useWizard({ initialKey: 3, steps }));

    expect(result.current[0].key).toEqual(3);
  });

  it('wizard goes to next step', () => {
    const { result } = renderHook(() => useWizard({ initialKey: 2, steps }));

    act(() => {
      result.current[1].next();
    });

    expect(result.current[0].key).toEqual(3);
  });

  it('wizard goes to previous step', () => {
    const { result } = renderHook(() => useWizard({ initialKey: 4, steps }));

    act(() => {
      result.current[1].previous();
    });

    expect(result.current[0].key).toEqual(3);
  });

  it('wizard goes to specific step', () => {
    const { result } = renderHook(() => useWizard({ initialKey: 1, steps }));

    act(() => {
      result.current[1].set(4);
    });

    expect(result.current[0].key).toEqual(4);
  });

  it('wizard goes to last step', () => {
    const { result } = renderHook(() => useWizard({ initialKey: 1, steps }));

    act(() => {
      result.current[1].last();
    });

    expect(result.current[0].key).toEqual(4);
  });

  it('wizard goes to first step', () => {
    const { result } = renderHook(() => useWizard({ initialKey: 3, steps }));

    act(() => {
      result.current[1].first();
    });

    expect(result.current[0].key).toEqual(1);
  });
});
