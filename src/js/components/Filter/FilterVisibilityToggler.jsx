import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowDownSLine, RiArrowUpSLine } from 'react-icons/ri';

import BadgeCount from 'components/Filter/BadgeCount';
import Translate from 'utils/Translate';

const FilterVisibilityToggler = ({ filtersHidden, amountFilled, setFiltersHidden }) => {
  const labelAttributes = filtersHidden
    ? { id: 'react.button.showFilters.label', defaultMessage: 'Show Filters' }
    : { id: 'react.button.hideFilters.label', defaultMessage: 'Hide Filters' };

  return (
    <button
      className="d-flex align-items-center hide-filters-button"
      type="button"
      onClick={() => setFiltersHidden(!filtersHidden)}
    >
      <span className="hide-filters-label">
        <Translate {...labelAttributes} />
      </span>
      { amountFilled > 0 && <BadgeCount count={amountFilled} />}
      { filtersHidden ? <RiArrowDownSLine /> : <RiArrowUpSLine /> }
    </button>
  );
};

export default FilterVisibilityToggler;

FilterVisibilityToggler.propTypes = {
  filtersHidden: PropTypes.bool.isRequired,
  amountFilled: PropTypes.number.isRequired,
  setFiltersHidden: PropTypes.func.isRequired,
};
