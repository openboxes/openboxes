import React from 'react';
import PropTypes from 'prop-types';

const DocumentButton = ({
  buttonIcon, buttonTitle, link, logo, target,
}) => (
  <a
    href={link}
    className="py-1 mb-1 btn btn-outline-secondary"
    target={target}
    rel="noopener noreferrer"
  >
    <span><i className={`pr-2 fa ${buttonIcon}`} />{buttonTitle} <img src={logo} alt={logo} /> </span>
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
  logo: PropTypes.string,
};

DocumentButton.defaultProps = {
  buttonIcon: 'fa-download',
  buttonTitle: 'Print Document',
  target: '_blank',
  logo: '',
};
