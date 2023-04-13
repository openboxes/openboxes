import React from 'react';

import PropTypes from 'prop-types';

import { AutosaveStatusDescription } from 'consts/autosaveStatuses';
import Translate from 'utils/Translate';

const AutosaveFeatureModalStatusParagraph = ({ status }) => (
  <p>
    <span className="bold">
      <Translate id={`react.autosaveFeatureModal.${status}.status.label`} defaultMessage={status} />
    </span> -
    &nbsp;<Translate id={`react.autosaveFeatureModal.${status}.status.description.label`} defaultMessage={`- ${AutosaveStatusDescription[status]}`} />
  </p>
);

export default AutosaveFeatureModalStatusParagraph;

AutosaveFeatureModalStatusParagraph.propTypes = {
  status: PropTypes.string.isRequired,
};
