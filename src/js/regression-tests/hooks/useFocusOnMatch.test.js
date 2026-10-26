import { renderHook } from '@testing-library/react-hooks';

import componentType from 'consts/componentType';
import useFocusOnMatch from 'hooks/useFocusOnMatch';

describe('useFocusOnMatch', () => {
  const inputRef = () => ({ current: { focus: jest.fn() } });
  const datePickerRef = () => ({
    current: { setOpen: jest.fn(), input: { focus: jest.fn() } },
  });

  const focusProps = (overrides) => ({
    columnId: 'quantity',
    fieldId: 'quantity',
    rowIndex: 2,
    fieldIndex: 2,
    ...overrides,
  });

  it('focuses the field on the focused row and column', () => {
    const ref = inputRef();

    renderHook(() => useFocusOnMatch(focusProps({ ref })));

    expect(ref.current.focus).toHaveBeenCalledTimes(1);
  });

  it.each([
    ['another row', { fieldIndex: 1 }],
    ['another column', { fieldId: 'lot' }],
    ['no focused column', { columnId: null, fieldId: null }],
  ])('does not focus the field of %s', (description, overrides) => {
    const ref = inputRef();

    renderHook(() => useFocusOnMatch(focusProps({ ref, ...overrides })));

    expect(ref.current.focus).not.toHaveBeenCalled();
  });

  it('opens a date picker and focuses its input', () => {
    const ref = datePickerRef();

    renderHook(() => useFocusOnMatch(focusProps({ ref, type: componentType.DATE_FIELD })));

    expect(ref.current.setOpen).toHaveBeenCalledWith(true);
    expect(ref.current.input.focus).toHaveBeenCalledTimes(1);
  });

  it('does not fail before the field is mounted', () => {
    expect(() => {
      renderHook(() => useFocusOnMatch(focusProps({ ref: { current: null } })));
      renderHook(() => useFocusOnMatch(focusProps({
        ref: { current: null }, type: componentType.DATE_FIELD,
      })));
    }).not.toThrow();
  });

  it('focuses the field when the focus moves to it', () => {
    const ref = inputRef();
    const { rerender } = renderHook((props) => useFocusOnMatch(props), {
      initialProps: focusProps({ ref, rowIndex: 1 }),
    });
    expect(ref.current.focus).not.toHaveBeenCalled();

    rerender(focusProps({ ref }));

    expect(ref.current.focus).toHaveBeenCalledTimes(1);
  });
});
