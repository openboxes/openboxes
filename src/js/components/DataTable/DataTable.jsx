import React from 'react';

import PropTypes from 'prop-types';
import ReactTable from 'react-table';
import ReactTablePropTypes from 'react-table/lib/propTypes';
import withFixedColumns from 'react-table-hoc-fixed-columns';

import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import TablePagination from 'components/DataTable/TablePagination';

import 'react-table/react-table.css';
// important: this line must be placed after react-table css import
import 'react-table-hoc-fixed-columns/lib/styles.css';
import 'components/DataTable/DataTable.scss';

const ReactTableFixedColumns = withFixedColumns(ReactTable);

const DataTable = React.forwardRef((props, ref) => {
  const {
    data, footerComponent, headerComponent, columns, className, totalData,
  } = props;

  const PaginationComponent = paginationProps => (
    <React.Fragment>
      { paginationProps.footerComponent && (
      <div className="app-react-table-footer d-flex p-2">
        {footerComponent()}
      </div>
      )}
      {data.length > 0 && <TablePagination{...paginationProps} totalData={totalData} /> }
    </React.Fragment>);

  return (
    <div className="app-react-table-wrapper">
      { headerComponent && (
        <div className="app-react-table-header d-flex p-2">
          {headerComponent()}
        </div>
      )}
      <ReactTableFixedColumns
        {...props}
        innerRef={ref}
        className={`app-react-table ${className} ${data.length === 0 ? 'hide-data' : ''}`}
        data={data}
        columns={columns}
        PaginationComponent={PaginationComponent}
        ThComponent={TableHeaderCell}
      />
    </div>
  );
});

DataTable.defaultProps = {
  footerComponent: undefined,
  headerComponent: undefined,
  sortable: false,
  resizable: false,
  className: '',
  multiSort: false,
  totalData: undefined,
};

DataTable.propTypes = {
  ...ReactTablePropTypes,
  footerComponent: PropTypes.func,
  headerComponent: PropTypes.func,
  sortable: PropTypes.bool,
  resizable: PropTypes.bool,
  multiSort: PropTypes.bool,
  className: PropTypes.string,
  totalData: PropTypes.number,
};


export default DataTable;

