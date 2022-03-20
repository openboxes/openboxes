import React from 'react';

import PropTypes from 'prop-types';
import ReactTable from 'react-table';

const BinTable = ({
  binData,
  binPageSize,
  binPage,
  determineAmountOfPages,
  binTotalCount,
  handlePageSizeChange,
  handlePageChange,
}) => {
  const binColumns = [
    {
      Header: 'Status',
      accessor: 'active',
      minWidth: 30,
      className: 'active-circle',
      headerClassName: 'header',
    },
    {
      Header: 'Name',
      accessor: 'name',
      className: 'cell',
      headerClassName: 'header text-align-left',
    },
    {
      Header: 'Bin Type',
      accessor: 'name',
      className: 'cell',
      headerClassName: 'header text-align-left',
    },
    {
      Header: 'Actions',
      minWidth: 20,
      accessor: 'actions',
      className: 'cell',
      headerClassName: 'header ',
    },
  ];
  const binDataToShow = binData.map(row => ({
    ...row,
    active: row.active === true ?
      <i className="fa fa-check-circle green-circle" aria-hidden="true" /> :
      <i className="fa fa-times-circle grey-circle" aria-hidden="true" />,
    actions:
  <div className="d-flex justify-content-center align-items-center">
    <i className="fa fa-pencil action-icons" aria-hidden="true" />
    <i className="fa fa-trash-o action-icons" aria-hidden="true" />
  </div>,
  }));

  return (
    <ReactTable
      data={binDataToShow}
      columns={binColumns}
      manual
      className="-striped -highlight zoneTable"
      resizable={false}
      sortable={false}
      multiSort={false}
      defaultPageSize={binPageSize}
      page={binPage}
      pages={determineAmountOfPages(binTotalCount, binPageSize)}
      onPageSizeChange={size => handlePageSizeChange(size, 'BIN_LOCATION')}
      onPageChange={(pageToSet) => {
        handlePageChange(pageToSet + 1, binPageSize, 'BIN_LOCATION');
      }}
      previousText={<i className="fa fa-chevron-left" aria-hidden="true" />}
      nextText={<i className="fa fa-chevron-right" aria-hidden="true" />}
      pageText=""
    />
  );
};

BinTable.propTypes = {
  binData: PropTypes.shape([]).isRequired,
  binPageSize: PropTypes.number.isRequired,
  binPage: PropTypes.number.isRequired,
  determineAmountOfPages: PropTypes.func.isRequired,
  binTotalCount: PropTypes.number.isRequired,
  handlePageSizeChange: PropTypes.func.isRequired,
  handlePageChange: PropTypes.func.isRequired,
};

export default BinTable;
