import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

import './styles.scss';

const Section = ({
  showTitle, title, children, className,
}) => (
  <div className={`v2-section ${className}`}>
    {showTitle && (
    <span className="v2-section-title text-uppercase">
      <Translate id={title?.label} defaultMessage={title?.defaultMessage} />
    </span>
    )}
    {children}
  </div>
);

export default Section;

Section.propTypes = {
  showTitle: PropTypes.bool,
  title: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }),
  children: PropTypes.node.isRequired,
  className: PropTypes.string,
};

Section.defaultProps = {
  showTitle: true,
  title: {
    label: '',
    defaultMessage: '',
  },
  className: '',
};
