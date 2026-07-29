import React from 'react';

import PropTypes from 'prop-types';

import ReceivingFilters from 'components/receivingV2/ReceivingFilters';
import ReceivingTable from 'components/receivingV2/ReceivingTable';
import useReceivingForm from 'hooks/receiving/v2/useReceivingForm';

import 'components/receivingV2/receiving.scss';

const ReceivingStep = ({ flushRef }) => {
  const {
    view,
    setView,
    putawayEnabled,
    setPutawayEnabled,
    table: { lineItemsState, columns },
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
    },
  } = useReceivingForm();

  // Handed up to the wizard, whose Next button awaits it before the step transition.
  // eslint-disable-next-line no-param-reassign
  flushRef.current = flush;

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
      />
    </div>
  );
};

ReceivingStep.propTypes = {
  // Filled with the autosave flush; the wizard's Next button awaits it before moving on.
  flushRef: PropTypes.shape({ current: PropTypes.func }).isRequired,
};

export default ReceivingStep;
