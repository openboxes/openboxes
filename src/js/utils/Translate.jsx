import React from 'react';

import PropTypes from 'prop-types';
import { Translate } from 'react-localize-redux';

export const translateWithDefaultMessage = translate => ((id, defaultMessage, data, options) =>
  translate(id, data, { ...options, onMissingTranslation: () => (defaultMessage || id) }));

const TranslateWrapper = ({
  id, defaultMessage, options = {}, ...props
}) => (
  <Translate
    id={id}
    options={{ ...options, onMissingTranslation: () => (defaultMessage || id) }}
    {...props}
  />
);

export default TranslateWrapper;

TranslateWrapper.propTypes = {
  id: PropTypes.string.isRequired,
  defaultMessage: PropTypes.string,
  options: PropTypes.shape({}),
};

TranslateWrapper.defaultProps = {
  defaultMessage: '',
  options: {},
};
