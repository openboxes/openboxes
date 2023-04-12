import React from 'react';

import PropTypes from 'prop-types';
import { RiInformationLine } from 'react-icons/all';

import Translate from 'utils/Translate';

const NewFeatureBarVersionBox = ({ versionLabel }) => (
  <div className="version-box">
    <RiInformationLine />
    <Translate id={versionLabel?.label} defaultMessage={versionLabel?.defaultLabel} />
  </div>
);

export default NewFeatureBarVersionBox;

NewFeatureBarVersionBox.propTypes = {
  versionLabel: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
  }).isRequired,
};
