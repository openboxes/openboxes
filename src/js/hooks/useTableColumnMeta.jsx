const useTableColumnMeta = (column) => {
  const meta = column.columnDef?.meta || {};

  return {
    hide: meta.hide || false,
    flexWidth: meta.flexWidth,
    // Lets the arrow keys move between the fields of this column.
    arrowNavigable: meta.arrowNavigable || false,
    className: meta.getCellContext?.().className || '',
  };
};

export default useTableColumnMeta;
