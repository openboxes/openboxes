import React from 'react';

import PropTypes from 'prop-types';

import { TableCell } from 'components/DataTable';
import ReceivingRowType from 'consts/receivingRowType';
import ChangesToggleCell from 'utils/cells/receiving/ChangesToggleCell';
import SplitItemCell from 'utils/cells/receiving/SplitItemCell';
import ValueCell from 'utils/cells/ValueCell';

/**
 * The product code cell. In table view this column also hosts the changes toggle
 * and the green arrow of split item rows.
 */
const ProductCodeCell = React.memo(({
  item, isPackingListView, isExpanded, onToggle,
}) => {
  // The changes toggle lives in the first pinned column: Code in table view,
  // the pack level group column in packing list view.
  if (item?.rowType === ReceivingRowType.TOGGLE) {
    return isPackingListView
      // An empty cell instead of null - the pinned cell wrapper has a white
      // background, so without a .rt-td inside it would stay white on row hover.
      ? <TableCell className="rt-td" />
      : (
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
        productCode={item?.productCode}
        withArrow={!isPackingListView}
      />
    );
  }
  return (
    <ValueCell
      value={item?.productCode}
      tooltipLabel={item?.productCode}
      className={item?.rowType === ReceivingRowType.REPLACED ? 'receiving-table__struck' : ''}
      label="react.receiving.code.label"
      defaultLabel="Code"
      truncate
    />
  );
});

ProductCodeCell.displayName = 'ProductCodeCell';

ProductCodeCell.propTypes = {
  item: PropTypes.shape({
    rowType: PropTypes.string,
    splitItemIds: PropTypes.arrayOf(PropTypes.string),
    isFirstSplitItem: PropTypes.bool,
    productCode: PropTypes.string,
  }),
  isPackingListView: PropTypes.bool,
  isExpanded: PropTypes.bool,
  onToggle: PropTypes.func.isRequired,
};

ProductCodeCell.defaultProps = {
  item: null,
  isPackingListView: false,
  isExpanded: false,
};

export default ProductCodeCell;
