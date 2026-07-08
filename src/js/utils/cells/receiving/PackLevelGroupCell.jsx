import React from 'react';

import PropTypes from 'prop-types';

import ReceivingRowType from 'consts/receivingRowType';
import ChangesToggleCell from 'utils/cells/receiving/ChangesToggleCell';
import SplitItemCell from 'utils/cells/receiving/SplitItemCell';
import ValueCell from 'utils/cells/ValueCell';

/**
 * The pack level group cell in packing list view. Besides the pack level value,
 * this column hosts the changes toggle and the green arrow of split item rows.
 */
const PackLevelGroupCell = React.memo(({ item, isExpanded, onToggle }) => {
  if (item?.rowType === ReceivingRowType.TOGGLE) {
    return (
      <ChangesToggleCell
        isExpanded={isExpanded}
        onToggle={onToggle}
        changeCount={item.splitItemIds.length}
      />
    );
  }
  if (item?.rowType === ReceivingRowType.SPLIT_ITEM) {
    return (
      <SplitItemCell
        isFirstSplitItem={item?.isFirstSplitItem}
        withArrow
        className="receiving-table__split-item-arrow-cell"
      />
    );
  }
  return (
    <ValueCell
      value={item?.packLevelGroup}
      tooltipLabel={item?.packLevelGroup}
      label="react.receiving.packLevel.label"
      defaultLabel="Pack Level"
      truncate
    />
  );
});

PackLevelGroupCell.displayName = 'PackLevelGroupCell';

PackLevelGroupCell.propTypes = {
  item: PropTypes.shape({
    rowType: PropTypes.string,
    splitItemIds: PropTypes.arrayOf(PropTypes.string),
    isFirstSplitItem: PropTypes.bool,
    packLevelGroup: PropTypes.string,
  }),
  isExpanded: PropTypes.bool,
  onToggle: PropTypes.func.isRequired,
};

PackLevelGroupCell.defaultProps = {
  item: null,
  isExpanded: false,
};

export default PackLevelGroupCell;
