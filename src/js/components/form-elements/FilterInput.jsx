import React from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { translateWithDefaultMessage } from 'utils/Translate';

const FilterInput = ({
  itemFilter, onChangeFunc, clearFunc, translate,
}) => (
  <div className="d-flex mr-auto justify-content-center align-items-center">
    <input
      value={itemFilter}
      onChange={onChangeFunc}
      className="float-left btn btn-outline-secondary btn-xs filter-input mr-1 mb-1"
      placeholder={translate('react.stockMovement.searchPlaceholder.label', 'Search...')}
    />
    {itemFilter &&
      <i
        role="button"
        className="fa fa-times-circle"
        style={{ color: 'grey', cursor: 'pointer' }}
        onClick={() => clearFunc()}
        onKeyPress={() => clearFunc()}
        tabIndex={0}
      />
    }
  </div>
);

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps)(FilterInput);


FilterInput.propTypes = {
  itemFilter: PropTypes.string.isRequired,
  onChangeFunc: PropTypes.func.isRequired,
  clearFunc: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
