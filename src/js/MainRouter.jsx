import React from 'react';
import PropTypes from 'prop-types';
import { renderToStaticMarkup } from 'react-dom/server';
import { withLocalize } from 'react-localize-redux';
import connect from 'react-redux/es/connect/connect';

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
        onMissingTranslation,
      },
    });

    this.props.addTranslationForLanguage(en, 'en');
    this.props.addTranslationForLanguage(fr, 'fr');
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale !== nextProps.locale) {
      this.props.setActiveLanguage(nextProps.locale);
      this.fetchLanguage(nextProps.locale);
    }
  }

  fetchLanguage(lang) {
    const url = `/openboxes/api/localizations?lang=${lang}`;
    return apiClient.get(url)
      .then((response) => {
        const { messages } = parseResponse(response.data);

        this.props.addTranslationForLanguage(messages, lang);
      });
  }

  render() {
    return (
      <Router />
    );
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
});

export default withLocalize(connect(mapStateToProps)(MainRouter));

MainRouter.propTypes = {
  initialize: PropTypes.func.isRequired,
  addTranslationForLanguage: PropTypes.func.isRequired,
  locale: PropTypes.string.isRequired,
  setActiveLanguage: PropTypes.func.isRequired,
};
