import React from 'react';

import { useWindowVirtualizer } from '@tanstack/react-virtual';

import MemoizedCountStepTable from 'components/cycleCount/toCountTab/MemoizedCountStepTable';

const VirtualizedTablesList = ({
  cycleCountIds,
  isStepEditable,
  isFormDisabled,
}) => {
  const tableVirtualizer = useWindowVirtualizer({
    count: cycleCountIds.length,
    // table with ~ 5 rows, average size of the count table
    estimateSize: () => 518,
    overscan: 2,
  });

  return (
    <div
      style={{
        height: `${tableVirtualizer.getTotalSize()}px`,
        position: 'relative',
      }}
    >
      {tableVirtualizer.getVirtualItems()
        .map((virtualRow) => {
          const id = cycleCountIds[virtualRow.index];

          return (
            <MemoizedCountStepTable
              id={id}
              key={id}
              start={virtualRow.start}
              index={virtualRow.index}
              measureElement={tableVirtualizer.measureElement}
              isStepEditable={isStepEditable}
              isFormDisabled={isFormDisabled}
            />
          );
        })}
    </div>
  );
};

export default VirtualizedTablesList;
