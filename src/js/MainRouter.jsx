import React from 'react';
import PropTypes from 'prop-types';
import { renderToStaticMarkup } from 'react-dom/server';
import { withLocalize } from 'react-localize-redux';


import Router from './components/Router';

import en from './en';
import fr from './fr';
import apiClient, { parseResponse } from './utils/apiClient';

const onMissingTranslation = ({ translationId }) => `${translationId}`;

class MainRouter extends React.Component {
  constructor(props) {
    super(props);

    this.props.initialize({
      languages: [
        { name: 'English', code: 'en' },
        { name: 'French', code: 'fr' },
      ],
      options: {
        renderToStaticMarkup,
        defaultLanguage: 'en',
        onMissingTranslation,
      },
    });

    this.props.addTranslationForLanguage(en, 'en');
    this.props.addTranslationForLanguage(fr, 'fr');
  }

  componentDidMount() {
    this.fetchEnglish();
    this.fetchFrench();
  }

  fetchEnglish() {
    const url = '/openboxes/api/localizations?lang=en';

    return apiClient.get(url)
      .then((response) => {
        const { messages } = parseResponse(response.data);

        this.props.addTranslationForLanguage(messages, 'en');
      });
  }

  fetchFrench() {
    const url = '/openboxes/api/localizations?lang=fr';

    return apiClient.get(url)
      .then((response) => {
        const { messages } = parseResponse(response.data);

        this.props.addTranslationForLanguage(messages, 'fr');
      });
  }

  render() {
    return (
      <Router />
    );
  }
}

export default withLocalize(MainRouter);

MainRouter.propTypes = {
  initialize: PropTypes.func.isRequired,
  addTranslationForLanguage: PropTypes.func.isRequired,
};
