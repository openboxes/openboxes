import React from 'react';

import ReceivingFilters from 'components/receivingV2/ReceivingFilters';
import ReceivingTable from 'components/receivingV2/ReceivingTable';
import useReceivingForm from 'hooks/receiving/v2/useReceivingForm';

import 'components/receivingV2/receiving.scss';

const ReceivingStep = () => {
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
      autofillQuantities,
      onSaveAndExit,
      removeSplitItem,
      loadReceipt,
      onLocationAutofill,
      autosaveStatus,
    },
    commentModal,
  } = useReceivingForm();

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
        removeSplitItem={removeSplitItem}
        loadReceipt={loadReceipt}
        commentModal={commentModal}
        onLocationAutofill={onLocationAutofill}
      />
    </div>
  );
};

export default ReceivingStep;
