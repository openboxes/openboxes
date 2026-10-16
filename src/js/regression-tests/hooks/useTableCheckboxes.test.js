import { act, renderHook } from '@testing-library/react-hooks';
import { useSelector } from 'react-redux';

import useTableCheckboxes from 'hooks/useTableCheckboxes';

jest.mock('react-redux', () => ({
  useSelector: jest.fn(),
}));

describe('useTableCheckboxes', () => {
  let session;

  // selectRow and selectHeaderCheckbox create their handlers with hooks, so the handlers are
  // created during the render, as the tables do.
  const renderCheckboxes = () => renderHook(() => {
    const checkboxes = useTableCheckboxes();
    return {
      ...checkboxes,
      toggleRow1: checkboxes.selectRow('row-1'),
      toggleRow2: checkboxes.selectRow('row-2'),
      toggleAll: checkboxes.selectHeaderCheckbox(['row-1', 'row-2', 'row-3']),
    };
  });

  beforeEach(() => {
    session = { currentLocation: { id: 'loc-1' } };
    useSelector.mockImplementation((selector) => selector({ session }));
  });

  it('starts with nothing checked', () => {
    const { result } = renderCheckboxes();

    expect(result.current.checkedCheckboxes).toEqual([]);
    expect(result.current.selectedCheckboxesAmount).toBe(0);
    expect(result.current.headerCheckboxProps).toEqual({ indeterminate: false, value: false });
  });

  it('checks and unchecks a row', () => {
    const { result } = renderCheckboxes();

    act(() => result.current.toggleRow1());
    act(() => result.current.toggleRow2());

    expect(result.current.checkedCheckboxes).toEqual(['row-1', 'row-2']);
    expect(result.current.isChecked('row-1')).toBe(true);
    expect(result.current.isChecked('row-3')).toBe(false);
    expect(result.current.headerCheckboxProps).toEqual({ indeterminate: true, value: true });

    act(() => result.current.toggleRow1());

    expect(result.current.checkedCheckboxes).toEqual(['row-2']);
    expect(result.current.selectedCheckboxesAmount).toBe(1);
  });

  it('marks the header again as unchecked once the last row is unchecked', () => {
    const { result } = renderCheckboxes();

    act(() => result.current.toggleRow1());
    act(() => result.current.toggleRow1());

    expect(result.current.headerCheckboxProps).toEqual({ indeterminate: false, value: false });
  });

  it('checks every row from the header, then unchecks them all', () => {
    const { result } = renderCheckboxes();

    act(() => result.current.toggleAll());
    expect(result.current.checkedCheckboxes).toEqual(['row-1', 'row-2', 'row-3']);
    expect(result.current.headerCheckboxProps.value).toBe(true);

    act(() => result.current.toggleAll());
    expect(result.current.checkedCheckboxes).toEqual([]);
    expect(result.current.headerCheckboxProps).toEqual({ indeterminate: false, value: false });
  });

  it('unchecks every row from the header when only some are checked', () => {
    const { result } = renderCheckboxes();
    act(() => result.current.toggleRow2());

    act(() => result.current.toggleAll());

    expect(result.current.checkedCheckboxes).toEqual([]);
  });

  it('resets the checkboxes', () => {
    const { result } = renderCheckboxes();
    act(() => result.current.toggleRow1());

    act(() => result.current.resetCheckboxes());

    expect(result.current.checkedCheckboxes).toEqual([]);
    expect(result.current.headerCheckboxProps).toEqual({ indeterminate: false, value: false });
  });

  it('lets the caller set the checked rows', () => {
    const { result } = renderCheckboxes();

    act(() => result.current.setCheckedCheckboxes(['row-3']));

    expect(result.current.isChecked('row-3')).toBe(true);
    expect(result.current.headerCheckboxProps.indeterminate).toBe(true);
  });

  it('unchecks everything when the location changes', () => {
    const { result, rerender } = renderCheckboxes();
    act(() => result.current.toggleRow1());

    session = { currentLocation: { id: 'loc-2' } };
    rerender();

    expect(result.current.checkedCheckboxes).toEqual([]);
  });
});
