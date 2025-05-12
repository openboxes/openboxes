const useTableColumnMeta = (column) => {
  const meta = column.columnDef?.meta || {};

  return {
    hide: meta.hide || false,
    flexWidth: meta.flexWidth,
    className: meta.getCellContext?.().className || '',
  };
};

export default useTableColumnMeta;
