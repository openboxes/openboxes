import { renderHook } from '@testing-library/react-hooks';

import useTableColumnMeta from 'hooks/useTableColumnMeta';

const metaOf = (column) => renderHook(() => useTableColumnMeta(column)).result.current;

describe('useTableColumnMeta', () => {
  it('reads the column meta', () => {
    const getCellContext = jest.fn(() => ({ className: 'text-right' }));

    expect(metaOf({
      columnDef: {
        meta: {
          hide: true, flexWidth: 120, arrowNavigable: true, getCellContext,
        },
      },
    })).toEqual({
      hide: true,
      flexWidth: 120,
      arrowNavigable: true,
      className: 'text-right',
    });
  });

  it('falls back to defaults for a column without meta', () => {
    const expected = {
      hide: false, flexWidth: undefined, arrowNavigable: false, className: '',
    };

    expect(metaOf({ columnDef: {} })).toEqual(expected);
    expect(metaOf({})).toEqual(expected);
  });

  it('uses no class when the cell context has none', () => {
    expect(metaOf({ columnDef: { meta: { getCellContext: () => ({}) } } }).className).toBe('');
  });
});
