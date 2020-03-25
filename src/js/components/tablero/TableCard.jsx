import React from 'react';
import PropTypes from 'prop-types';

const TableCard = props => (
  <div>
    <span>Hey, Im a {props.name}</span>
  </div>
);

TableCard.propTypes = {
  name: PropTypes.string.isRequired,
};

TableCard.defaultProps = {
};

export default TableCard;
