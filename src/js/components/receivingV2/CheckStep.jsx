import React from 'react';

import ConfirmReceiptFilters from 'components/receivingV2/ConfirmReceiptFilters';
import ConfirmReceiptInfo from 'components/receivingV2/ConfirmReceiptInfo';
import useConfirmReceiptForm from 'hooks/receiving/v2/useConfirmReceiptForm';

import 'components/receivingV2/receiving.scss';

const CheckStep = () => {
  const { control } = useConfirmReceiptForm();

  return (
    <div className="receiving-container confirm-receipt">
      <ConfirmReceiptInfo control={control} />
      <ConfirmReceiptFilters />
    </div>
  );
};

export default CheckStep;
