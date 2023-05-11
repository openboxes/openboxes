import React from 'react';

import PropTypes from 'prop-types';
import { RiInformationLine } from 'react-icons/all';
import { useDispatch } from 'react-redux';

import { showInfoBarModal } from 'actions';
import { InfoBarVersionBoxVariant } from 'consts/infoBar';
import Translate from 'utils/Translate';

const InfoBarVersionBox = ({
  versionLabel, withIcon, variant, name,
}) => {
  const dispatch = useDispatch();

  return (
    <button
      className={`version-box version-box-${variant}`}
      onClick={() => dispatch(showInfoBarModal(name))}
    >
      {withIcon && <RiInformationLine />}
      <Translate id={versionLabel?.label} defaultMessage={versionLabel?.defaultLabel} />
    </button>
  );
};

export default InfoBarVersionBox;

InfoBarVersionBox.propTypes = {
  name: PropTypes.string.isRequired,
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
