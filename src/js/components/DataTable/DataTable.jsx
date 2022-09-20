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

const DataTable = (props) => {
  const {
    data, footerComponent, headerComponent, columns,
  } = props;

  const PaginationComponent = paginationProps => (
    <React.Fragment>
      { paginationProps.footerComponent && (
      <div className="app-react-table-footer d-flex p-2">
        {footerComponent()}
      </div>
      )}
      {data.length > 0 && <TablePagination {...paginationProps} /> }
    </React.Fragment>);

  return (
    <div className="app-react-table-wrapper" style={{ maxWidth: '1000px' }}>
      { headerComponent && (
        <div className="app-react-table-header d-flex p-2">
          {headerComponent()}
        </div>
      )}
      <ReactTableFixedColumns
        {...props}
        className="app-react-table"
        data={data}
        sortable={false}
        resizable={false}
        columns={columns}
        defaultPageSize={data.length > 0 ? props.defaultPageSize : 0}
        PaginationComponent={PaginationComponent}
        ThComponent={TableHeaderCell}
      />
    </div>
  );
};

DataTable.defaultProps = {
  footerComponent: undefined,
  headerComponent: undefined,
};

DataTable.propTypes = {
  ...ReactTablePropTypes,
  footerComponent: PropTypes.func,
  headerComponent: PropTypes.func,
};


export default DataTable;

