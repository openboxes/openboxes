import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const CycleCountDiscrepancyInfoBar = ({ outOfStockItems }) => (
  <div className="d-flex justify-content-between flex-column draft-modal">
    {outOfStockItems.map((item) => (
      <div key={item.id}>
        <Translate
          id="react.cycleCount.modal.outOfStockDiscrepancyFound.label"
          defaultMessage=""
          data={{
            binLocation: item.binLocation?.name,
            lotNumber: item.inventoryItem?.lotNumber,
            quantityOnHand: item.quantityOnHand,
            quantityCounted: item.quantityCounted,
          }}
        />
      </div>
    ))}
  </div>
);

export default CycleCountDiscrepancyInfoBar;

CycleCountDiscrepancyInfoBar.propTypes = {
  outOfStockItems: PropTypes.arrayOf(PropTypes.shape({}).isRequired).isRequired,
};
