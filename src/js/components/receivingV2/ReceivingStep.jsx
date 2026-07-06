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
      loading, updateLineItem, onSaveAndExit,
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
        onSaveAndExit={onSaveAndExit}
      />
      <ReceivingTable
        lineItemsState={lineItemsState}
        columns={columns}
        loading={loading}
        updateLineItem={updateLineItem}
        commentModal={commentModal}
      />
    </div>
  );
};

export default ReceivingStep;
