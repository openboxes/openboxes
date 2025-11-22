import React, { memo } from 'react';

import CountStepTable from 'components/cycleCount/toCountTab/CountStepTable';

const VirtualizedCountStepTable = memo(
  ({
    id,
    start,
    index,
    measureElement,
    isStepEditable,
    isFormDisabled,
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
      />
    </div>
  ),
);

export default VirtualizedCountStepTable;
