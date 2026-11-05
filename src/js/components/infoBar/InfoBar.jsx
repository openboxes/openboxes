import React from 'react';

import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/ri';
import { useDispatch } from 'react-redux';

import { closeInfoBar } from 'actions';
import InfoBarRedirect from 'components/infoBar/InfoBarRedirect';
import InfoBarTitle from 'components/infoBar/InfoBarTitle';
import InfoBarVersionBox from 'components/infoBar/InfoBarVersionBox';
import useTranslation from 'hooks/useTranslation';

const InfoBar = ({
  name,
  versionLabel = null,
  title,
  isCloseable = true,
  hasModalToDisplay = true,
  redirect = null,
}) => {
  useTranslation('infoBar');
  const dispatch = useDispatch();

  return (
    <div className="info-bar">
      <div className="d-flex justify-content-center gap-8 align-items-center">
        <InfoBarVersionBox versionLabel={versionLabel} name={name} />
        <InfoBarTitle title={title} name={name} hasModalToDisplay={hasModalToDisplay} />
        {redirect && <InfoBarRedirect redirect={redirect} />}
      </div>
      {isCloseable && <RiCloseFill onClick={() => dispatch(closeInfoBar(name))} cursor="pointer" />}
    </div>
  );
};

export default InfoBar;

InfoBar.propTypes = {
  name: PropTypes.string.isRequired,
  title: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
  }).isRequired,
  versionLabel: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
  }),
  isCloseable: PropTypes.bool,
  hasModalToDisplay: PropTypes.bool,
  redirect: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
    link: PropTypes.string,
  }),
};
