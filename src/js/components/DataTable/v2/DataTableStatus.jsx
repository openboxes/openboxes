import React from 'react';

import PropTypes from 'prop-types';

import useTranslate from 'hooks/useTranslate';

const DataTableStatus = ({ defaultMessage, label, shouldDisplay }) => {
  const translate = useTranslate();

  return shouldDisplay ? (
    <div className="rt-noData">
      {translate(label, defaultMessage)}
    </div>
  ) : null;
};

export default DataTableStatus;

DataTableStatus.propTypes = {
  defaultMessage: PropTypes.string.isRequired,
  label: PropTypes.string.isRequired,
  shouldDisplay: PropTypes.bool,
};

DataTableStatus.defaultProps = {
  shouldDisplay: false,
};
