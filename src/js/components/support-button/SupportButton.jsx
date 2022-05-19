import React, { useEffect } from 'react';

import { faLifeRing } from '@fortawesome/free-regular-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import axios from 'axios';
import PropTypes from 'prop-types';
import { useChat } from 'react-live-chat-loader';
import { connect } from 'react-redux';

import Translate from 'utils/Translate';

import './SupportButton.scss';

function configureBeacon() {
  axios.get('/openboxes/api/helpscout/configuration/')
    .then((response) => {
      window.Beacon('config', response.data);
    });
}

const SupportButton = ({ text, defaultText, locale }) => {
  const [, loadChat] = useChat();

  // instantiate `window` object when page is first loaded
  useEffect(() => {
    loadChat({ open: false });
    configureBeacon();
  }, []);

  // reload beacon configuration whenever locale changes
  useEffect(() => {
    configureBeacon();
  }, [locale]);

  const toggleOpenChat = () => window.Beacon('toggle');

  return (
    <button
      type="button"
      className="btn btn-helpscout ml-1 mr-1"
      onClick={toggleOpenChat}
    >
      <FontAwesomeIcon icon={faLifeRing} />
      &nbsp;
      <Translate id={text} defaultMessage={defaultText} />
    </button>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
});

SupportButton.propTypes = {
  text: PropTypes.string,
  defaultText: PropTypes.string,
  locale: PropTypes.string,
};

SupportButton.defaultProps = {
  text: 'Help',
  defaultText: 'Help',
  locale: 'en',
};

export default connect(mapStateToProps)(SupportButton);
