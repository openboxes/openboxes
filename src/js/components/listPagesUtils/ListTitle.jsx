import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const ListTitle = ({ label }) => (
  <span className="d-flex align-self-center title">
    <Translate id={label.id} defaultMessage={label.defaultMessage} />
  </span>
);

export default ListTitle;

ListTitle.propTypes = {
  label: PropTypes.shape({
    id: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }).isRequired,
};
