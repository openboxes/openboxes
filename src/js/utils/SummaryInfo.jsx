import React from 'react';

import PropTypes from 'prop-types';

import 'utils/SummaryInfo.scss';

/**
 * Generic component for displaying summary info.
 * Accepts an array of objects, each describing a single column with a `title`
 * and its `data`, rendered as evenly spaced, divider-separated cells.
 */
const SummaryInfo = ({ data }) => (
  <div className="summary-info">
    {data.map(({ title, data: value }) => (
      <div className="summary-info__item" key={title}>
        <span className="summary-info__title">{title}</span>
        <span className="summary-info__data">{value}</span>
      </div>
    ))}
  </div>
);

export default SummaryInfo;

SummaryInfo.propTypes = {
  data: PropTypes.arrayOf(PropTypes.shape({
    title: PropTypes.node.isRequired,
    data: PropTypes.node,
  })).isRequired,
};
