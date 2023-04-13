import React from 'react';

import PropTypes from 'prop-types';
import { RiInformationLine } from 'react-icons/all';

import { InfoBarVersionBoxVariant } from 'consts/infoBar';
import Translate from 'utils/Translate';

const InfoBarVersionBox = ({ versionLabel, withIcon, variant }) => (
  <div className={`version-box version-box-${variant}`}>
    {withIcon && <RiInformationLine />}
    <Translate id={versionLabel?.label} defaultMessage={versionLabel?.defaultLabel} />
  </div>
);

export default InfoBarVersionBox;

InfoBarVersionBox.propTypes = {
  versionLabel: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
  }).isRequired,
  withIcon: PropTypes.bool,
  variant: PropTypes.string,
};

InfoBarVersionBox.defaultProps = {
  withIcon: true,
  variant: InfoBarVersionBoxVariant.OUTLINED,
};
