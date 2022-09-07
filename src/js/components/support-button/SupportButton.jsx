import React, { useEffect } from 'react';

import axios from 'axios';
import PropTypes from 'prop-types';
import { RiQuestionLine } from 'react-icons/ri';
import { useChat } from 'react-live-chat-loader';
import { connect } from 'react-redux';

import './SupportButton.scss';

const SupportButton = ({ locale }) => {
  const [, loadChat] = useChat();

  useEffect(() => {
    loadChat({ open: false }); // instantiate `window` object
  }, []);

  useEffect(() => {
    axios.get('/openboxes/api/helpscout/configuration/')
      .then((response) => {
        window.Beacon('destroy');
        window.Beacon('init', response.data.localizedHelpScoutKey);
        window.Beacon('config', response.data);
      });
  }, [locale]);


  const toggleOpenChat = () => window.Beacon('toggle');

  return (
    <RiQuestionLine
      onClick={toggleOpenChat}
    />
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
});

SupportButton.propTypes = {
  locale: PropTypes.string.isRequired,
};


export default connect(mapStateToProps)(SupportButton);
