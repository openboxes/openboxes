import React from 'react';

import PropTypes from 'prop-types';
import { RiQuestionLine } from 'react-icons/ri';
import { LiveChatLoaderProvider } from 'react-live-chat-loader';
import { connect } from 'react-redux';

import SupportButton from 'components/support-button/SupportButton';

const HelpScout = ({ isHelpScoutEnabled, localizedHelpScoutKey, ...props }) => (
  isHelpScoutEnabled ?
    <LiveChatLoaderProvider provider="helpScout" providerKey={localizedHelpScoutKey}>
      <SupportButton {...props} />
    </LiveChatLoaderProvider>
    : <RiQuestionLine />
);

const mapStateToProps = state => ({
  localizedHelpScoutKey: state.session.localizedHelpScoutKey,
  isHelpScoutEnabled: state.session.isHelpScoutEnabled,
});

export default connect(mapStateToProps)(HelpScout);


HelpScout.propTypes = {
  localizedHelpScoutKey: PropTypes.string,
  isHelpScoutEnabled: PropTypes.bool,
  text: PropTypes.string,
  className: PropTypes.string,
};

HelpScout.defaultProps = {
  text: '',
  className: '',
  localizedHelpScoutKey: '',
  isHelpScoutEnabled: false,
};
