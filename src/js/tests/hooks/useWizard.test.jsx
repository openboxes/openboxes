import React from 'react';

import { act, renderHook } from '@testing-library/react-hooks';
import { MemoryRouter } from 'react-router-dom';

import useWizard from 'hooks/useWizard';

describe('Changing steps', () => {
  const STEP_KEYS = {
    FIRST: 'FIRST',
    SECOND: 'SECOND',
    THIRD: 'THIRD',
    FOURTH: 'FOURTH',
  };

  const steps = [
    {
      key: STEP_KEYS.FIRST,
      Component: () => (<div>First component</div>),
    },
    {
      key: STEP_KEYS.SECOND,
      Component: () => (<div>Second component</div>),
    },
    {
      key: STEP_KEYS.THIRD,
      Component: () => (<div>Third component</div>),
    },
    {
      key: STEP_KEYS.FOURTH,
      Component: () => (<div>Fourth component</div>),
    },
  ];

  it('should start with the initial key', () => {
    const {
      result,
    } = renderHook(
      () => useWizard({ initialKey: STEP_KEYS.THIRD, steps }),
      { wrapper: MemoryRouter },
    );

    expect(result.current[0].key).toEqual(STEP_KEYS.THIRD);
  });

  it('should go to next step', () => {
    const { result } = renderHook(
      () => useWizard({ initialKey: STEP_KEYS.SECOND, steps }),
      { wrapper: MemoryRouter },
    );

    act(() => {
      result.current[1].next();
    });

    expect(result.current[0].key).toEqual(STEP_KEYS.THIRD);
  });

  it('should go to previous step', () => {
    const {
      result,
    } = renderHook(
      () => useWizard({ initialKey: STEP_KEYS.FOURTH, steps }),
      { wrapper: MemoryRouter },
    );

    act(() => {
      result.current[1].previous();
    });

    expect(result.current[0].key).toEqual(STEP_KEYS.THIRD);
  });

  it('should go to specific step', () => {
    const {
      result,
    } = renderHook(
      () => useWizard({ initialKey: STEP_KEYS.FIRST, steps }),
      { wrapper: MemoryRouter },
    );

    act(() => {
      result.current[1].navigateToStep(STEP_KEYS.FOURTH);
    });

    expect(result.current[0].key).toEqual(STEP_KEYS.FOURTH);
  });

  it('should go to last step', () => {
    const {
      result,
    } = renderHook(
      () => useWizard({ initialKey: STEP_KEYS.FIRST, steps }),
      { wrapper: MemoryRouter },
    );

    act(() => {
      result.current[1].last();
    });

    expect(result.current[0].key).toEqual(STEP_KEYS.FOURTH);
  });

  it('should go to first step', () => {
    const {
      result,
    } = renderHook(
      () => useWizard({ initialKey: STEP_KEYS.THIRD, steps }),
      { wrapper: MemoryRouter },
    );
    act(() => {
      result.current[1].first();
    });

    expect(result.current[0].key).toEqual(STEP_KEYS.FIRST);
  });
});
