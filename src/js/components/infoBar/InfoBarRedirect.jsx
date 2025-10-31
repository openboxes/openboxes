import React from 'react';

import PropTypes from 'prop-types';

import useTranslate from 'hooks/useTranslate';

const InfoBarRedirect = ({ redirect }) => {
  const translate = useTranslate();
  const {
    label,
    defaultLabel,
    link,
  } = redirect;

  return (
    <div className="info-bar-redirect">
      <a href={link} target="_blank" rel="noreferrer">
        {translate(label, defaultLabel)}
      </a>
    </div>
  );
};

export default InfoBarRedirect;

InfoBarRedirect.propTypes = {
  redirect: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
    link: PropTypes.string,
  }).isRequired,
};
