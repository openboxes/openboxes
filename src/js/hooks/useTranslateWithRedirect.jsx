import React from 'react';

import useTranslate from 'hooks/useTranslate';

/**
 * Custom hook that provides a translation function with redirect capabilities.
 *
 * This hook extends the basic translation functionality by allowing specific phrases
 * in the translated text to be replaced with clickable links that redirect to specified URLs.
 *
 * Use only for translations that require embedding links within the text, in other cases
 * prefer using the standard useTranslate hook for better performance.
 *
 * @returns {Function} A function that translates text and replaces specified phrases with links.
 */
const useTranslateWithRedirect = () => {
  const translate = useTranslate();

  /**
   * Translates a label and replaces a specific phrase in the translated string
   * with a clickable redirect link.
   *
   * Supports multiple redirects, allowing different phrases in the same
   * translation to be replaced with different links.
   *
   * @param {Object} params
   * @param {Object} params.options - Options for translation interpolation
   * @param {string} params.label - Translation key
   * @param {string} params.defaultLabel - Fallback text used when the translation key is missing
   * @param {Array<Object>} params.redirects - List of redirect configurations
   * @param {string} params.redirects[].phrase - Exact phrase in the translated string that should
   * become a link
   * @param {string} params.redirects[].redirectTo - Target URL for the generated anchor tag
   *
   * @returns {string | React.ReactNode} - Translated text with the phrase replaced by a hyperlink
   */
  return ({
    label, defaultLabel, options = {}, redirects = [],
  }) => {
    const content = translate(label, defaultLabel, options);

    redirects.forEach(({ phrase }) => {
      if (!content.includes(phrase)) {
        throw new Error(`The phrase "${phrase}" was not found in the translation`);
      }
    });

    const stringifiedHTML = redirects.reduce((acc, { phrase, redirectTo }) => {
      const anchorTag = `<a href="${redirectTo}" target="_blank">${phrase}</a>`;
      return acc.replace(phrase, anchorTag);
    }, content);

    return (
      <span dangerouslySetInnerHTML={{ __html: stringifiedHTML }} />
    );
  };
};

export default useTranslateWithRedirect;
