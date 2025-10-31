import React from 'react';

import PropTypes from 'prop-types';
import { Translate } from 'react-localize-redux';

/**
 * @callback TranslationCallback
 * @param {string} id - The translation code of the message to translate
 * which is localed in messages.properties.
 * @param {string} [defaultMessage] - The default message to use if the translation is missing.
 * @param {Object} [data] - Optional data for variable replacements in dynamic translations.
 * @param {Object} [options] - Additional options for the translation.
 * @param {string} [options.language]  Optionally pass a language code to force
 * Translate to render a specific language.
 * @param {boolean} [options.renderInnerHtml] Override initialize renderInnerHtml option
 * for translation.
 * @returns {string} - The translated message.
 */

/**
 * Creates a function that translates a message with a default if the translation is missing.
 * @param {function} translate - The translation function provided by react-localize-redux.
 * @returns {TranslationCallback} - The translated message function.
 */
export const translateWithDefaultMessage = (translate) => (

  (id, defaultMessage, data, options) =>
    translate(id, data, {
      ...options,
      onMissingTranslation: () => (defaultMessage || id),
    })
);

/**
 * A wrapper component for the Translate component that provides default message handling.
 * @param {Object} props - The props for the TranslateWrapper component.
 * @param {string} props.id - The ID of the message to translate.
 * @param {string} props.defaultMessage - The default message to use if the translation is missing.
 * @param {string} props.data - Optional data for variable replacements in dynamic translations.
 * @returns {JSX.Element} - The Translate component with default message handling.
 */
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
