import { useLayoutEffect } from 'react';

// Opens the table on its right-most columns - on the first rows, and again when the set of
// columns changes, because those can arrive later.
const useTableScrollToEnd = ({
  bottomScrollbarRef, initialHorizontalScroll, hasRows, tableWidth,
}) => {
  useLayoutEffect(() => {
    if (initialHorizontalScroll !== 'end' || !hasRows) {
      return;
    }

    const bottomScrollbar = bottomScrollbarRef.current;

    bottomScrollbar.scrollLeft = bottomScrollbar.scrollWidth;
  }, [initialHorizontalScroll, hasRows, tableWidth]);
};

export default useTableScrollToEnd;
