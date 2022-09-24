import React from 'react';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const StockListHeader = () => {
  const goToAddStockList = () => {
    window.location.href = '/openboxes/requisitionTemplate/create?type=STOCK';
  };

  return (
    <div className="d-flex list-page-header">
      <span className="d-flex align-self-center title">
        <Translate id="react.stockMovement.stocklistList.label" defaultMessage="Stocklist List" />
      </span>
      <div className="d-flex justify-content-end buttons align-items-center">
        <Button
          defaultLabel="Add stocklist"
          label="react.stockListManagement.addStockList.label"
          onClickAction={goToAddStockList}
        />
      </div>
    </div>
  );
};

export default StockListHeader;
