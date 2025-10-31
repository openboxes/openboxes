import React from 'react';

import PropTypes from 'prop-types';

import TablePagination from 'components/DataTable/TablePagination';

const DataTableFooter = ({
  footerComponent,
  pagination,
  onPageChange,
  onPageSizeChange,
  canPrevious,
  canNext,
  pages,
  pageSizeSelectOptions,
  totalData,
}) => (
  <>
    {footerComponent && (
    <div className="table-footer">
      {footerComponent()}
    </div>
    )}
    <div className="pagination-bottom">
      <TablePagination
        page={pagination.pageIndex}
        onPageChange={onPageChange}
        onPageSizeChange={onPageSizeChange}
        pageSize={pagination.pageSize}
        totalData={totalData}
        resolvedData={[]}
        canPrevious={canPrevious()}
        canNext={canNext()}
        pages={pages}
        pageSizeOptions={pageSizeSelectOptions}
        className="table-pagination-bottom"
      />
    </div>
  </>
);

export default DataTableFooter;

DataTableFooter.propTypes = {
  footerComponent: PropTypes.func,
  pagination: PropTypes.shape({
    pageSize: PropTypes.number.isRequired,
    pageIndex: PropTypes.number.isRequired,
  }).isRequired,
  onPageChange: PropTypes.func.isRequired,
  onPageSizeChange: PropTypes.func.isRequired,
  canPrevious: PropTypes.func.isRequired,
  canNext: PropTypes.func.isRequired,
  pages: PropTypes.number.isRequired,
  totalData: PropTypes.number.isRequired,
  pageSizeSelectOptions: PropTypes.arrayOf(PropTypes.number).isRequired,
};

DataTableFooter.defaultProps = {
  footerComponent: null,
};
