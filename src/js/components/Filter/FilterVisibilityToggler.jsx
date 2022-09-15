import React from 'react';

import PropTypes from 'prop-types';
import { RiArrowDownSLine, RiArrowUpSLine } from 'react-icons/all';

import BadgeCount from 'components/Filter/BadgeCount';
import Translate from 'utils/Translate';

const FilterVisibilityToggler = ({ filtersHidden, amountFilled, setFiltersHidden }) => (
  <button
    className="d-flex align-items-center hide-filters-button"
    type="button"
    onClick={() => setFiltersHidden(!filtersHidden)}
  >
    {filtersHidden ?
      <React.Fragment>
        <span className="hide-filters-label">
          <Translate id="react.showFilters.label" defaultMessage="Show Filters" />
        </span>
        <BadgeCount count={amountFilled} />
        <RiArrowDownSLine />
      </React.Fragment>
      :
      <React.Fragment>
        <span className="hide-filters-label">
          <Translate id="react.hideFilters.label" defaultMessage="Hide Filters" />
        </span>
        <BadgeCount count={amountFilled} />
        <RiArrowUpSLine />
      </React.Fragment>
      }
  </button>
);

export default FilterVisibilityToggler;

FilterVisibilityToggler.propTypes = {
  filtersHidden: PropTypes.bool.isRequired,
  amountFilled: PropTypes.number.isRequired,
  setFiltersHidden: PropTypes.func.isRequired,
};
