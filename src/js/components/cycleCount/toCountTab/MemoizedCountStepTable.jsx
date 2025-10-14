import React, { memo } from 'react';

import CountStepTable from 'components/cycleCount/toCountTab/CountStepTable';

const MemoizedCountStepTable = memo(
  ({
    id,
    start,
    index,
    measureElement,
    isStepEditable,
    isFormDisabled,
    isAssignCountModalOpen,
    closeAssignCountModal,
    assignCountModalData,
  }) => (
    <div
      key={id}
      data-index={index}
      ref={measureElement}
      style={{
        position: 'absolute',
        top: 0,
        transform: `translateY(${start}px)`,
        width: '100%',
      }}
    >
      <CountStepTable
        id={id}
        isStepEditable={isStepEditable}
        isFormDisabled={isFormDisabled}
        isAssignCountModalOpen={isAssignCountModalOpen}
        closeAssignCountModal={closeAssignCountModal}
        assignCountModalData={assignCountModalData}
      />
    </div>
  ),
  (prev, next) =>
    prev.id === next.id
    && prev.start === next.start
    && prev.isStepEditable === next.isStepEditable
    && prev.isFormDisabled === next.isFormDisabled
    && prev.isAssignCountModalOpen === next.isAssignCountModalOpen
    && prev.assignCountModalData === next.assignCountModalData,
);

export default MemoizedCountStepTable;
