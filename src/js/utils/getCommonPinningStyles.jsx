const getCommonPinningStyles = (
  column,
  flexWidth,
  isScreenWiderThanTable,
  dataLength,
  loading,
  isRowDisabled = false,
) => {
  const isPinned = column.getIsPinned();
  const isLastLeftPinnedColumn = isPinned === 'left' && column.getIsLastColumn('left');
  const isFirstRightPinnedColumn = isPinned === 'right' && column.getIsFirstColumn('right');

  return {
    boxShadow: (isLastLeftPinnedColumn || isFirstRightPinnedColumn) ? '0 0 15px #00000040' : undefined,
    clipPath: (isLastLeftPinnedColumn && 'inset(0 -15px 0 0)')
      || (isFirstRightPinnedColumn && 'inset(0 0 0 -15px)')
      || undefined,
    marginRight: isLastLeftPinnedColumn ? '5px' : undefined,
    marginLeft: isFirstRightPinnedColumn ? '5px' : undefined,
    left: isPinned === 'left' ? `${column.getStart('left')}px` : undefined,
    right: isPinned === 'right' ? `${column.getAfter('right')}px` : undefined,
    position: isPinned && !isScreenWiderThanTable && dataLength && !loading && 'sticky',
    flex: flexWidth || column.getSize(),
    width: flexWidth || column.getSize(),
    zIndex: isPinned ? 1 : 0,
    // #f8f9fa matches the `bg-light` class applied to disabled rows
    background: isPinned && (isRowDisabled ? '#f8f9fa' : 'white'),
  };
};

export default getCommonPinningStyles;
