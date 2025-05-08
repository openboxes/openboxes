const getCommonPinningStyles = (column, flexWidth, isScreenWiderThanTable) => {
  const isPinned = column.getIsPinned();
  const isLastLeftPinnedColumn = isPinned === 'left' && column.getIsLastColumn('left');

  let boxShadow;
  let clipPath;
  let marginRight;
  if (isLastLeftPinnedColumn) {
    boxShadow = '0 0 15px #00000040';
    clipPath = 'inset(0 -15px 0 0)';
    marginRight = '5px';
  } else {
    boxShadow = undefined;
    clipPath = undefined;
    marginRight = undefined;
  }

  return {
    boxShadow,
    clipPath,
    marginRight,
    left: isPinned === 'left' ? `${column.getStart('left')}px` : undefined,
    position: isPinned && !isScreenWiderThanTable && 'sticky',
    flex: flexWidth || column.getSize(),
    width: !isScreenWiderThanTable && (flexWidth || column.getSize()),
    zIndex: isPinned ? 1 : 0,
    background: isPinned && 'white',
  };
};

export default getCommonPinningStyles;
