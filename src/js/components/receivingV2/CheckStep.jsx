import React, { useEffect } from 'react';

import PropTypes from 'prop-types';

import ConfirmReceiptFilters from 'components/receivingV2/ConfirmReceiptFilters';
import ConfirmReceiptInfo from 'components/receivingV2/ConfirmReceiptInfo';
import ConfirmReceiptTable from 'components/receivingV2/ConfirmReceiptTable';
import useConfirmReceiptForm from 'hooks/receiving/v2/useConfirmReceiptForm';

import 'components/receivingV2/receiving.scss';

const CheckStep = ({ completeReceiptRef, setNextDisabled }) => {
  const {
    onCompleteReceipt,
    isCompleteDisabled,
    onSaveAndExit,
    control,
    view,
    table,
    shipmentLineItemsState,
    filters,
    loading,
    commentModal,
    cancelRemaining,
  } = useConfirmReceiptForm();

  // Handed up to the wizard, whose Complete Receipt button runs it.
  // eslint-disable-next-line no-param-reassign
  completeReceiptRef.current = onCompleteReceipt;

  useEffect(() => {
    setNextDisabled(isCompleteDisabled);
  }, [isCompleteDisabled]);

  return (
    <div className="receiving-container confirm-receipt" data-testid="check-step">
      <ConfirmReceiptInfo
        control={control}
        shipmentLineItemsState={shipmentLineItemsState}
        canceledReceiptItemIds={cancelRemaining.ids}
      />
      <ConfirmReceiptFilters
        view={view}
        updateFilterParams={filters.updateFilterParams}
        clearFilterParams={filters.clearFilterParams}
        onCancelAllRemaining={cancelRemaining.selectAll}
        onSaveAndExit={onSaveAndExit}
        onResetSort={filters.resetSort}
      />
      <ConfirmReceiptTable
        lineItemsState={table.lineItemsState}
        columns={table.columns}
        loading={loading}
        commentModal={commentModal}
        cancelRemaining={cancelRemaining}
        sort={table.sort}
        order={table.order}
      />
    </div>
  );
};

CheckStep.propTypes = {
  completeReceiptRef: PropTypes.shape({ current: PropTypes.func }).isRequired,
  setNextDisabled: PropTypes.func.isRequired,
};

export default CheckStep;
