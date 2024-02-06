import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

import './styles.scss';

const Section = ({ title, children }) => (
  <div className="v2-section">
    <span className="text-uppercase">
      <Translate id={title.label} defaultMessage={title.defaultMessage} />
    </span>
    {children}
  </div>
);

export default Section;

Section.propTypes = {
  title: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }).isRequired,
  children: PropTypes.node.isRequired,
};
