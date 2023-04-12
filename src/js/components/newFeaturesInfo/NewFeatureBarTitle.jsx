import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const NewFeatureBarTitle = ({ title }) => (
    <>
      <Translate id={title?.label} defaultMessage={title?.defaultLabel} />
      <span
        className="read-more-label"
      >
        <Translate id="react.newFeature.readMore.label" defaultMessage="Read more" />
      </span>
    </>
);

export default NewFeatureBarTitle;


NewFeatureBarTitle.propTypes = {
  title: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
  }).isRequired,
};
