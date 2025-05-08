const getCommonPinningStyles = (column, flexWidth, isScreenWider) => {
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
    right: isPinned === 'right' ? `${column.getAfter('right')}px` : undefined,
    position: isPinned && !isScreenWider && 'sticky',
    flex: flexWidth || column.getSize(),
    width: flexWidth || column.getSize(),
    zIndex: isPinned ? 1 : 0,
    background: isPinned && 'white',
  };
};

export default getCommonPinningStyles;
