import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const InfoBarTitle = ({ title }) => (
    <>
      <Translate id={title?.label} defaultMessage={title?.defaultLabel} />
      <span
        className="read-more-label"
      >
        <Translate id="react.infoBar.readMore.label" defaultMessage="Read more" />
      </span>
    </>
);

export default InfoBarTitle;


InfoBarTitle.propTypes = {
  title: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
  }).isRequired,
};
