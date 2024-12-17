import React from 'react';

import { Tooltip } from 'react-tippy';

import Button from 'components/form-elements/Button';
import useTranslate from 'hooks/useTranslate';

const AllProductsTabFooter = () => {
  const translate = useTranslate();

  return (
    <div className="d-flex gap-12 pl-4 p-1 all-products-tab-footer">
      <Tooltip
        theme="transparent"
        duration="250"
        html={(
          <div className="custom-tooltip">
            {translate(
              'react.cycleCount.table.refreshSuggestionTooltip.label',
              'Click here to manually refresh the All Products table data. It will erase the current suggestion ranking and generate a new one.',
            )}
          </div>
        )}
      >
        <Button
          className="suggestion-button"
          variant="transparent"
          defaultLabel="Refresh suggestion"
          label="react.cycleCount.table.refreshSuggestion"
        />
      </Tooltip>
      <p>
        {translate('react.cycleCount.table.lastRefresh', 'Last refresh')}
        {' '}
        <span>12/01/2024</span>
      </p>
      <p>
        {translate('react.cycleCount.table.refreshPeriod', 'Refresh period')}
        {' '}
        <span>12/01/2024-12/07/2024</span>
      </p>
    </div>
  );
};

export default AllProductsTabFooter;
