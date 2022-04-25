import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { useChat } from 'react-live-chat-loader';

import Translate from 'utils/Translate';

import './SupportButton.scss';

const SupportButton = ({ buttonColor, text, defaultText }) => {
  const [, loadChat] = useChat();
  useEffect(() => {
    loadChat({ open: false });
  }, []);

  const toggleOpenChat = () => window.Beacon('toggle');
  return (
    <button
      type="button"
      className="btn ml-1 btn-light"
      style={{ backgroundColor: buttonColor }}
      onClick={toggleOpenChat}
    >
      <Translate id={text} defaultMessage={defaultText} />
    </button>
  );
};

SupportButton.propTypes = {
  buttonColor: PropTypes.string,
  text: PropTypes.string,
  defaultText: PropTypes.string,
};

SupportButton.defaultProps = {
  buttonColor: undefined,
  text: 'Help',
  defaultText: 'Help',
};

export default SupportButton;
