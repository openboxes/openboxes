import React from 'react';

import PropTypes from 'prop-types';

import ConfirmReceiptFilters from 'components/receivingV2/ConfirmReceiptFilters';
import ConfirmReceiptInfo from 'components/receivingV2/ConfirmReceiptInfo';
import ConfirmReceiptTable from 'components/receivingV2/ConfirmReceiptTable';
import useConfirmReceiptForm from 'hooks/receiving/v2/useConfirmReceiptForm';

import 'components/receivingV2/receiving.scss';

const CheckStep = ({ completeReceiptRef }) => {
  const {
    onCompleteReceipt,
    onSaveAndExit,
    control,
    table,
    lineItemsState,
    filters,
    loading,
    commentModal,
    cancelRemaining,
  } = useConfirmReceiptForm();

  // Handed up to the wizard, whose Complete Receipt button runs it.
  // eslint-disable-next-line no-param-reassign
  completeReceiptRef.current = onCompleteReceipt;

  return (
    <div className="receiving-container confirm-receipt">
      <ConfirmReceiptInfo control={control} lineItemsState={lineItemsState} />
      <ConfirmReceiptFilters
        updateFilterParams={filters.updateFilterParams}
        clearFilterParams={filters.clearFilterParams}
        onCancelAllRemaining={cancelRemaining.selectAll}
        onSaveAndExit={onSaveAndExit}
      />
      <ConfirmReceiptTable
        lineItemsState={table.lineItemsState}
        columns={table.columns}
        loading={loading}
        commentModal={commentModal}
        cancelRemaining={cancelRemaining}
      />
    </div>
  );
};

CheckStep.propTypes = {
  completeReceiptRef: PropTypes.shape({ current: PropTypes.func }).isRequired,
};

export default CheckStep;
