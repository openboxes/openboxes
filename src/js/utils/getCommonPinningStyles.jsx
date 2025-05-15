const getCommonPinningStyles = (
  column,
  flexWidth,
  isScreenWiderThanTable,
  dataLength,
  loading,
) => {
  const isPinned = column.getIsPinned();
  const isLastLeftPinnedColumn = isPinned === 'left' && column.getIsLastColumn('left');

  return {
    boxShadow: isLastLeftPinnedColumn ? '0 0 15px #00000040' : undefined,
    clipPath: isLastLeftPinnedColumn ? 'inset(0 -15px 0 0)' : undefined,
    marginRight: isLastLeftPinnedColumn ? '5px' : undefined,
    left: isPinned === 'left' ? `${column.getStart('left')}px` : undefined,
    position: isPinned && !isScreenWiderThanTable && dataLength && !loading && 'sticky',
    flex: flexWidth || column.getSize(),
    width: flexWidth || column.getSize(),
    zIndex: isPinned ? 1 : 0,
    background: isPinned && 'white',
  };
};

export default getCommonPinningStyles;
