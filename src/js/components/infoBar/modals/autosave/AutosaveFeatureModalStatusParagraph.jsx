import React from 'react';

import PropTypes from 'prop-types';

import { AutosaveIcon, AutosaveStatusDescription } from 'consts/autosaveStatuses';
import Translate from 'utils/Translate';

const AutosaveFeatureModalStatusParagraph = ({ status }) => (
  <p id="feature-modal-paragraph">
    <div className={`feature-modal-paragraph-${status}`}>{AutosaveIcon[status.toUpperCase()]}</div>
    <span className="bold">
      <Translate id={`react.autosaveFeatureModal.${status}.status.label`} defaultMessage={status} />
    </span> -
    &nbsp;<Translate
      id={`react.autosaveFeatureModal.${status}.status.description.label`}
      defaultMessage={`- ${AutosaveStatusDescription[status.toUpperCase()]}`}
    />
  </p>
);

export default AutosaveFeatureModalStatusParagraph;

AutosaveFeatureModalStatusParagraph.propTypes = {
  status: PropTypes.string.isRequired,
};
