import React from 'react';

import moment from 'moment';
import PropTypes from 'prop-types';

import DataTable from 'components/DataTable/v2/DataTable';
import useTranslate from 'hooks/useTranslate';

import 'components/cycleCount/cycleCount.scss';

const CheckStepTable = ({
  product,
  dateCounted,
  tableData,
  columns,
}) => {
  const translate = useTranslate();
  return (
    <div className="list-page-list-section">
      <p className="check-step-title pt-4 pl-4">
        {product?.productCode}
        {' '}
        {product?.name}
      </p>
      <div className="py-4 pl-4 d-flex align-items-center">
        <div className="d-flex align-items-center">
          <span className="check-step-label mr-2">
            {translate('react.cycleCount.dateCounted.label', 'Date counted')}
          </span>
          {' '}
          <span>{moment(dateCounted).format('DD/MM/YYYY')}</span>
        </div>
        {/* this is mocked because the backend doesn't return this value yet */}
        <div className="d-flex align-items-center ml-5">
          <span className="check-step-label mr-2">
            {translate('react.cycleCount.countedBy.label', 'Counted by')}
          </span>
          {' '}
          <span>John Smith</span>
        </div>
      </div>
      <div className="mx-4 check-step-table">
        <DataTable
          columns={columns}
          data={tableData}
          totalCount={tableData.length}
          filterParams={{}}
          disablePagination
        />
      </div>
    </div>
  );
};

export default CheckStepTable;

CheckStepTable.propTypes = {
  product: PropTypes.shape({
    productCode: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  dateCounted: PropTypes.string.isRequired,
  tableData: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
  columns: PropTypes.arrayOf(
    PropTypes.shape({}),
  ).isRequired,
};
