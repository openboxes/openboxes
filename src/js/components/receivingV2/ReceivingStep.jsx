import React from 'react';

import ReceivingFilters from 'components/receivingV2/ReceivingFilters';
import ReceivingTable from 'components/receivingV2/ReceivingTable';
import useReceivingForm from 'hooks/receiving/v2/useReceivingForm';

import 'components/receivingV2/receiving.scss';

const ReceivingStep = () => {
  const {
    table: { lineItems, columns },
    actions: { loading },
  } = useReceivingForm();

  return (
    <div className="receiving-container">
      <ReceivingFilters />
      <ReceivingTable lineItems={lineItems} columns={columns} loading={loading} />
    </div>
  );
};

export default ReceivingStep;
