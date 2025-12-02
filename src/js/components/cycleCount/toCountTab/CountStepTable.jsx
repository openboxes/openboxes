import React from 'react';

import PropTypes from 'prop-types';

import AddNewRecordFooter from 'components/cycleCount/tableFooter/AddNewRecordFooter';
import CountedByHeader from 'components/cycleCount/tableHeader/CountedByHeader';
import DateCountedHeader from 'components/cycleCount/tableHeader/DateCountedHeader';
import ProductDataHeader from 'components/cycleCount/tableHeader/ProductDataHeader';
import DataTable from 'components/DataTable/v2/DataTable';
import useCountStepTable from 'hooks/cycleCount/useCountStepTable';

import 'components/cycleCount/cycleCount.scss';

const CountStepTable = ({
  id,
  isStepEditable,
  isFormDisabled,
}) => {
  const {
    columns,
    cycleCountItemsTotalCount,
    cycleCountItemIds,
  } = useCountStepTable({
    cycleCountId: id,
    isStepEditable,
    isFormDisabled,
  });

  return (
    <>
      <div className="list-page-list-section">
        <ProductDataHeader
          cycleCountId={id}
        />
        <div className="pt-3 pl-4 d-flex align-items-center">
          <DateCountedHeader
            isStepEditable={isStepEditable}
            isFormDisabled={isFormDisabled}
            cycleCountId={id}
          />
          <CountedByHeader
            isStepEditable={isStepEditable}
            isFormDisabled={isFormDisabled}
            cycleCountId={id}
          />
        </div>
        <div className="mx-4 count-step-table">
          <DataTable
            columns={columns}
            data={cycleCountItemIds}
            totalCount={cycleCountItemsTotalCount}
            disablePagination
          />
        </div>
        <AddNewRecordFooter
          cycleCountId={id}
          isStepEditable={isStepEditable}
          isFormDisabled={isFormDisabled}
        />
      </div>
    </>
  );
};

export default CountStepTable;

CountStepTable.propTypes = {
  id: PropTypes.string.isRequired,
  isStepEditable: PropTypes.bool.isRequired,
  isFormDisabled: PropTypes.bool.isRequired,
};
