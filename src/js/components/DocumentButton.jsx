import React from 'react';
import PropTypes from 'prop-types';

function handleClick(onClick, event, link) {
  event.preventDefault();
  const newWindow = window.open('', '_blank');
  onClick()
    .then(() => { newWindow.location.href = link; });
}

const DocumentButton = ({
  buttonIcon, buttonTitle, link, target, disabled, onClick,
}) => (
  <a
    href={link}
    className={`py-1 mb-1 btn btn-outline-secondary ${disabled ? 'disabled' : ''}`}
    target={target}
    rel="noopener noreferrer"
    onClick={event => handleClick(onClick, event, link)}
  >
    <span><i className={`pr-2 fa ${buttonIcon}`} />{buttonTitle}</span>
  </a>
);

export default DocumentButton;

DocumentButton.propTypes = {
  /** String with font awesome icon class name */
  buttonIcon: PropTypes.string,
  /** String button title */
  buttonTitle: PropTypes.string,
  /** Link to document that will be set in href */
  link: PropTypes.string.isRequired,
  /** Target attribute, that specifies where to open the linked document (_blank by default) */
  target: PropTypes.string,
  disabled: PropTypes.bool.isRequired,
  onClick: PropTypes.func.isRequired,
};

DocumentButton.defaultProps = {
  buttonIcon: 'fa-download',
  buttonTitle: 'Print Document',
  target: '_blank',
};
