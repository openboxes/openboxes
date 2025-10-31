import React from 'react';

import PropTypes from 'prop-types';

import useTranslate from 'hooks/useTranslate';
import Translate from 'utils/Translate';

const CycleCountStockDiscrepancyInfoBar = ({ outOfStockItems }) => {
  const translate = useTranslate();
  return (
    <div className="d-flex justify-content-between flex-column draft-modal position-relative t-1">
      {outOfStockItems.map((item) => (
        <div key={item.id}>
          <Translate
            id="react.cycleCount.modal.outOfStockDiscrepancyFound.label"
            defaultMessage={`Inventory was updated for bin ${item.binLocation?.name ?? 'DEFAULT'}, lot ${item.inventoryItem?.lotNumber || 'NO LOT'} during your count. Discrepancy found QOH = ${item.quantityOnHand} and quantity counted = ${item.quantityCounted}`}
            data={{
              binLocation: item.binLocation?.name ?? translate('react.cycleCount.defaultBin.label', 'DEFAULT'),
              lotNumber: item.inventoryItem?.lotNumber || translate('react.cycleCount.emptyLotNumber.label', 'NO LOT'),
              quantityOnHand: item.quantityOnHand,
              quantityCounted: item.quantityCounted,
            }}
          />
        </div>
      ))}
    </div>
  );
};

export default CycleCountStockDiscrepancyInfoBar;

CycleCountStockDiscrepancyInfoBar.propTypes = {
  outOfStockItems: PropTypes.arrayOf(PropTypes.shape({}).isRequired).isRequired,
};
