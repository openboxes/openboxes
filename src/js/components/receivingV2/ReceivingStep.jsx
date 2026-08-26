import React, { useEffect } from 'react';

import PropTypes from 'prop-types';

import ReceivingFilters from 'components/receivingV2/ReceivingFilters';
import ReceivingTable from 'components/receivingV2/ReceivingTable';
import useReceivingForm from 'hooks/receiving/v2/useReceivingForm';

import 'components/receivingV2/receiving.scss';

const ReceivingStep = ({ flushRef, validateBeforeNextRef, setNextDisabled }) => {
  const {
    view,
    setView,
    putawayEnabled,
    setPutawayEnabled,
    table: {
      lineItemsState, columns, sort, order,
    },
    next: { isNextDisabled, validateBeforeNext },
    actions: {
      loading,
      receiptId,
      updateLineItem,
      updateLineItemComment,
      autofillQuantities,
      onSaveAndExit,
      removeSplitItem,
      loadReceipt,
      flush,
      onLocationAutofill,
      autosaveStatus,
      resetSort,
      updateFilterParams,
      clearFilterParams,
    },
  } = useReceivingForm();

  // Handed up to the wizard, whose Next button awaits it before the step transition.
  // eslint-disable-next-line no-param-reassign
  flushRef.current = flush;
  // Validation the wizard runs before the flush - it may ask the user to confirm and answer
  // that the transition should not happen.
  // eslint-disable-next-line no-param-reassign
  validateBeforeNextRef.current = validateBeforeNext;

  useEffect(() => {
    setNextDisabled(isNextDisabled);
  }, [isNextDisabled]);

  return (
    <div className="receiving-container">
      <ReceivingFilters
        view={view}
        onViewChange={setView}
        putawayEnabled={putawayEnabled}
        onPutawayChange={setPutawayEnabled}
        onAutofillQuantities={autofillQuantities}
        onSaveAndExit={onSaveAndExit}
        autosaveStatus={autosaveStatus}
        onResetSort={resetSort}
        updateFilterParams={updateFilterParams}
        clearFilterParams={clearFilterParams}
      />
      <ReceivingTable
        lineItemsState={lineItemsState}
        columns={columns}
        loading={loading}
        receiptId={receiptId}
        updateLineItem={updateLineItem}
        updateLineItemComment={updateLineItemComment}
        removeSplitItem={removeSplitItem}
        loadReceipt={loadReceipt}
        onLocationAutofill={onLocationAutofill}
        sort={sort}
        order={order}
      />
    </div>
  );
};

ReceivingStep.propTypes = {
  // Filled with the autosave flush; the wizard's Next button awaits it before moving on.
  flushRef: PropTypes.shape({ current: PropTypes.func }).isRequired,
  // Filled with the step validation; the wizard's Next button awaits it before the flush.
  validateBeforeNextRef: PropTypes.shape({ current: PropTypes.func }).isRequired,
  // Reports to the wizard whether its Next button should be disabled.
  setNextDisabled: PropTypes.func.isRequired,
};

export default ReceivingStep;
