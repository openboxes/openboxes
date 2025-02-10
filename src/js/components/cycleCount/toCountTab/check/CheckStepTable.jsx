import React from 'react';

import moment from 'moment';

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
      <p className="count-step-title pt-4 pl-4">
        {product?.productCode}
        {' '}
        {product?.name}
      </p>
      <div className="py-4 pl-4 d-flex align-items-center">
        <div className="d-flex align-items-center">
          <span className="count-step-label mr-2">
            {translate('react.cycleCount.dateCounted.label', 'Date counted')}
          </span>
          {' '}
          <span>{moment(dateCounted).format('DD/MM/YYYY')}</span>
        </div>
        <div className="d-flex align-items-center ml-5">
          <span className="count-step-label mr-2">
            {translate('react.cycleCount.countedBy.label', 'Counted by')}
          </span>
          {' '}
          <span>John Smith</span>
        </div>
      </div>
      <div className="mx-4 count-step-table">
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
