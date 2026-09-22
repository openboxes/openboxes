import { useLayoutEffect, useRef } from 'react';

// A second horizontal scrollbar under the table header, within reach of the rows being worked
// on. CSS cannot move or duplicate a native scrollbar, so this is the scrollbar of an empty
// box, kept in step with the table. Returns the ref to attach to it.
const useTableTopScrollbar = ({
  bottomScrollbarRef, showTopScrollbar, hasRows, tableWidth, isScreenWiderThanTable,
}) => {
  const topScrollbarRef = useRef(null);

  // Under the header, as wide as the table and with as much to scroll through, which is what
  // gives both bars the same range and the same thumb.
  const layoutBar = () => {
    const bottomScrollbar = bottomScrollbarRef.current;
    const topScrollbar = topScrollbarRef.current;

    topScrollbar.style.top = `${bottomScrollbar.querySelector('.rt-thead').offsetHeight}px`;
    topScrollbar.style.width = `${bottomScrollbar.clientWidth}px`;
    topScrollbar.style.setProperty('--table-content-width', `${bottomScrollbar.scrollWidth}px`);
  };

  // Whichever one the user scrolls, the other follows.
  const syncScroll = ({ target }) => {
    const bottomScrollbar = bottomScrollbarRef.current;

    if (target === bottomScrollbar) {
      topScrollbarRef.current.scrollLeft = target.scrollLeft;
      return;
    }

    bottomScrollbar.scrollLeft = target.scrollLeft;
  };

  useLayoutEffect(() => {
    if (!showTopScrollbar) {
      return undefined;
    }

    const bottomScrollbar = bottomScrollbarRef.current;
    const topScrollbar = topScrollbarRef.current;

    layoutBar();
    topScrollbar.scrollLeft = bottomScrollbar.scrollLeft;
    bottomScrollbar.addEventListener('scroll', syncScroll);
    topScrollbar.addEventListener('scroll', syncScroll);
    window.addEventListener('resize', layoutBar);

    return () => {
      bottomScrollbar.removeEventListener('scroll', syncScroll);
      topScrollbar.removeEventListener('scroll', syncScroll);
      window.removeEventListener('resize', layoutBar);
    };
  }, [showTopScrollbar, hasRows, tableWidth, isScreenWiderThanTable]);

  return topScrollbarRef;
};

export default useTableTopScrollbar;
