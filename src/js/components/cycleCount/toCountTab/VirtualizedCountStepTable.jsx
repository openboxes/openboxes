import React, { memo } from 'react';

import PropTypes from 'prop-types';

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

VirtualizedCountStepTable.propTypes = {
  id: PropTypes.string.isRequired,
  start: PropTypes.number.isRequired,
  index: PropTypes.number.isRequired,
  measureElement: PropTypes.func.isRequired,
  isStepEditable: PropTypes.bool.isRequired,
  isFormDisabled: PropTypes.bool.isRequired,
};
