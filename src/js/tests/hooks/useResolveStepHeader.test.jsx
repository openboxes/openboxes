import { act, renderHook } from '@testing-library/react-hooks';
import moment from 'moment';

import useResolveStepHeader from 'hooks/cycleCount/useResolveStepHeader';

describe('useResolveStepHeader', () => {
  const baseUser = {
    id: 'user-1',
    firstName: 'John',
    lastName: 'Doe',
    name: 'John Doe',
    label: 'John Doe',
    value: 'user-1',
  };

  const newUser = {
    id: 'user-2',
    firstName: 'Alice',
    lastName: 'Wonder',
    name: 'Alice Wonder',
    label: 'Alice Wonder',
    value: 'user-2',
  };

  const props = {
    id: 'random-id',
    initialDateRecounted: moment('2025-08-25T09:00:00Z'),
    initialRecountedBy: baseUser,
    initialDefaultRecountedBy: baseUser,
    updateRecountedDate: jest.fn(),
    assignRecountedBy: jest.fn(() => jest.fn()),
  };

  it('initializes with correct values', () => {
    const { result } = renderHook(() => useResolveStepHeader(props));

    expect(result.current.dateRecounted).toBe(props.initialDateRecounted);
    expect(result.current.recountedByMeta).toEqual({
      id: baseUser.id,
      value: baseUser.value,
      label: baseUser.label,
      name: baseUser.name,
    });
    expect(result.current.defaultRecountedByMeta).toEqual({
      id: baseUser.id,
      value: baseUser.value,
      label: baseUser.label,
      name: baseUser.name,
    });
  });

  it('updates dateRecounted and calls updateRecountedDate', () => {
    const { result } = renderHook(() => useResolveStepHeader(props));
    const newDate = moment('2025-08-26T10:00:00Z');

    act(() => result.current.handleDateRecountedChange(newDate));

    expect(result.current.dateRecounted).toBe(newDate);
    expect(props.updateRecountedDate).toHaveBeenCalledWith(newDate);
  });

  it('updates recountedBy and defaultRecountedBy and calls assignRecountedBy', () => {
    const { result } = renderHook(() => useResolveStepHeader(props));
    const assignFn = jest.fn();
    props.assignRecountedBy.mockReturnValue(assignFn);

    act(() => result.current.handleRecountedByChange(newUser));

    expect(result.current.recountedByMeta).toEqual({
      id: newUser.id,
      value: newUser.value,
      label: newUser.label,
      name: newUser.name,
    });
    expect(result.current.defaultRecountedByMeta).toEqual({
      id: newUser.id,
      value: newUser.value,
      label: newUser.label,
      name: newUser.name,
    });
    expect(props.assignRecountedBy).toHaveBeenCalledWith('random-id');
    expect(assignFn).toHaveBeenCalledWith(newUser);
  });

  it('resets internal state when initial props change', () => {
    const { result, rerender } = renderHook(
      (hookProps) => useResolveStepHeader(hookProps),
      { initialProps: props },
    );

    const newProps = {
      ...props,
      initialDateRecounted: moment('2025-08-27T12:00:00Z'),
      initialRecountedBy: newUser,
      initialDefaultRecountedBy: newUser,
    };

    rerender(newProps);

    expect(result.current.dateRecounted).toBe(newProps.initialDateRecounted);
    expect(result.current.recountedByMeta).toEqual({
      id: newUser.id,
      value: newUser.value,
      label: newUser.label,
      name: newUser.name,
    });
    expect(result.current.defaultRecountedByMeta).toEqual({
      id: newUser.id,
      value: newUser.value,
      label: newUser.label,
      name: newUser.name,
    });
  });

  it('handles null initialRecountedBy and initialDefaultRecountedBy', () => {
    const { result } = renderHook(() =>
      useResolveStepHeader({
        ...props,
        initialRecountedBy: null,
        initialDefaultRecountedBy: null,
      }));

    expect(result.current.recountedByMeta).toBeNull();
    expect(result.current.defaultRecountedByMeta).toBeNull();
  });

  it('generates label and name when label is missing', () => {
    const userWithoutLabel = {
      id: 'user-3',
      firstName: 'No',
      lastName: 'Label',
      name: 'No Label',
      value: 'user-3',
    };

    const { result } = renderHook(() =>
      useResolveStepHeader({
        ...props,
        initialRecountedBy: userWithoutLabel,
        initialDefaultRecountedBy: userWithoutLabel,
      }));

    expect(result.current.recountedByMeta).toEqual({
      id: 'user-3',
      value: 'user-3',
      label: 'No Label',
      name: 'No Label',
    });
  });
});
