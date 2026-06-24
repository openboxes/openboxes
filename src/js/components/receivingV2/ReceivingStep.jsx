import React from 'react';

import ReceivingFilters from 'components/receivingV2/ReceivingFilters';
import ReceivingTable from 'components/receivingV2/ReceivingTable';
import useReceivingForm from 'hooks/receiving/v2/useReceivingForm';

import 'components/receivingV2/receiving.scss';

const ReceivingStep = () => {
  const {
    view,
    setView,
    table: { lineItemsState, columns },
    actions: { loading, updateLineItem },
  } = useReceivingForm();

  return (
    <div className="receiving-container">
      <ReceivingFilters view={view} onViewChange={setView} />
      <ReceivingTable
        lineItemsState={lineItemsState}
        columns={columns}
        loading={loading}
        updateLineItem={updateLineItem}
      />
    </div>
  );
};

export default ReceivingStep;
