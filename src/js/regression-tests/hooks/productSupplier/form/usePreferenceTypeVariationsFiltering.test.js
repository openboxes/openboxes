import { act, renderHook } from '@testing-library/react-hooks';

import usePreferenceTypeVariationsFiltering from 'hooks/productSupplier/form/usePreferenceTypeVariationsFiltering';

const render = (props) => renderHook(() => usePreferenceTypeVariationsFiltering(props));

describe('usePreferenceTypeVariationsFiltering', () => {
  const updatedRows = [
    { comments: 'filled' },
    { comments: '' },
    { destinationParty: { id: 'org' } },
  ];
  // Row 1 has an error but is empty, so it does not count as invalid
  const errors = { 0: { comments: 'bad' }, 1: { comments: 'bad' } };

  it('counts only dirty rows with errors as invalid', () => {
    const { result } = render({ errors, updatedRows });

    expect(result.current.invalidRowCount).toBe(1);
    expect(result.current.isFiltered).toBe(false);
  });

  it('lets every row through until filtering is turned on', () => {
    const { result } = render({ errors, updatedRows });

    expect([0, 1, 2].map((index) => result.current.getFilterMethod({}, { _index: index })))
      .toEqual([true, true, true]);
    expect(result.current.triggerFiltering()).toEqual([{ value: false }]);

    act(() => result.current.setIsFiltered(true));

    expect([0, 1, 2].map((index) => Boolean(result.current.getFilterMethod({}, { _index: index }))))
      .toEqual([true, false, false]);
    expect(result.current.triggerFiltering()).toEqual([{ value: true }]);
  });

  it('keeps a minimum page size of four rows', () => {
    const { result } = render({ errors, updatedRows });
    expect(result.current.tablePageSize).toBe(4);

    const manyRows = Array.from({ length: 6 }, () => ({ comments: 'x' }));
    const { result: many } = render({ errors: {}, updatedRows: manyRows });
    expect(many.current.tablePageSize).toBe(6);

    act(() => many.current.setIsFiltered(true));
    expect(many.current.tablePageSize).toBe(4);
  });
});
