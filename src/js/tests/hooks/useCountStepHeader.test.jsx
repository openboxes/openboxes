import { act, renderHook } from '@testing-library/react-hooks';
import moment from 'moment';
import useCountStepHeader from 'hooks/cycleCount/useCountStepHeader';

describe('useCountStepHeader', () => {
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
    initialDateCounted: moment('2025-08-25T09:00:00Z'),
    initialCountedBy: baseUser,
    initialDefaultCountedBy: baseUser,
    updateDateCounted: jest.fn(() => jest.fn()),
    assignCountedBy: jest.fn(() => jest.fn()),
  };

  it('initializes with correct values', () => {
    const { result } = renderHook(() => useCountStepHeader(props));

    expect(result.current.dateCounted).toBe(props.initialDateCounted);
    expect(result.current.countedByMeta).toEqual({
      id: baseUser.id,
      value: baseUser.value,
      label: baseUser.label,
      name: baseUser.name,
    });
    expect(result.current.defaultCountedByMeta).toEqual({
      id: baseUser.id,
      value: baseUser.value,
      label: baseUser.label,
      name: baseUser.name,
    });
  });

  it('updates dateCounted and calls updateDateCounted', () => {
    const { result } = renderHook(() => useCountStepHeader(props));
    const newDate = moment('2025-08-26T10:00:00Z');

    act(() => result.current.handleDateCountedChange(newDate));

    expect(result.current.dateCounted).toBe(newDate.format());
    expect(props.updateDateCounted).toHaveBeenCalledWith(newDate);
  });

  it('updates countedBy and defaultCountedBy and calls assignCountedBy', () => {
    const { result } = renderHook(() => useCountStepHeader(props));
    const assignFn = jest.fn();
    props.assignCountedBy.mockReturnValue(assignFn);

    act(() => result.current.handleCountedByChange(newUser));

    expect(result.current.countedByMeta).toEqual({
      id: newUser.id,
      value: newUser.value,
      label: newUser.label,
      name: newUser.name,
    });
    expect(result.current.defaultCountedByMeta).toEqual({
      id: newUser.id,
      value: newUser.value,
      label: newUser.label,
      name: newUser.name,
    });
    expect(props.assignCountedBy).toHaveBeenCalledWith('random-id');
    expect(assignFn).toHaveBeenCalledWith(newUser);
  });

  it('resets internal state when initial props change', () => {
    const { result, rerender } = renderHook(
      (hookProps) => useCountStepHeader(hookProps),
      { initialProps: props },
    );

    const newProps = {
      ...props,
      initialDateCounted: moment('2025-08-27T12:00:00Z'),
      initialCountedBy: newUser,
      initialDefaultCountedBy: newUser,
    };

    rerender(newProps);

    expect(result.current.dateCounted).toBe(newProps.initialDateCounted);
    expect(result.current.countedByMeta).toEqual({
      id: newUser.id,
      value: newUser.value,
      label: newUser.label,
      name: newUser.name,
    });
    expect(result.current.defaultCountedByMeta).toEqual({
      id: newUser.id,
      value: newUser.value,
      label: newUser.label,
      name: newUser.name,
    });
  });

  it('handles null initialCountedBy and initialDefaultCountedBy', () => {
    const { result } = renderHook(() =>
      useCountStepHeader({
        ...props,
        initialCountedBy: null,
        initialDefaultCountedBy: null,
      }));

    expect(result.current.countedByMeta).toBeNull();
    expect(result.current.defaultCountedByMeta).toBeNull();
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
      useCountStepHeader({
        ...props,
        initialCountedBy: userWithoutLabel,
        initialDefaultCountedBy: userWithoutLabel,
      }));

    expect(result.current.countedByMeta).toEqual({
      id: 'user-3',
      value: 'user-3',
      label: 'No Label',
      name: 'No Label',
    });
  });
});
