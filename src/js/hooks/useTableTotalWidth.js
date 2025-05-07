import { useMemo } from 'react';

const useTableTotalWidth = (items) => useMemo(() => {
  if (!items || items.length === 0) {
    return 0;
  }

  return items.reduce((sum, item) => {
    if (item.column.columnDef?.meta?.hide) {
      return sum;
    }
    const width = item.column.columnDef.meta?.width || 0;
    return sum + width;
  }, 0);
}, [items]);

export default useTableTotalWidth;
