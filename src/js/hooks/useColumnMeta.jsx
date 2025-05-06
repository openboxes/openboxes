const useColumnMeta = (column) => {
  const meta = column.columnDef?.meta || {};

  return {
    hide: meta.hide || false,
    width: meta.width,
    flexWidth: meta.flexWidth || 1,
    fixed: meta.fixed || false,
    className: meta.getCellContext?.().className || '',
  };
};

export default useColumnMeta;
