import React from 'react';
import PropTypes from 'prop-types';
import { renderToStaticMarkup } from 'react-dom/server';
import { withLocalize } from 'react-localize-redux';
import connect from 'react-redux/es/connect/connect';

import Router from './components/Router';

import en from './en';
import fr from './fr';
import es from './es';
import ar from './ar';
import { fetchTranslations, fetchSessionInfo } from './actions';

const onMissingTranslation = ({ translationId }) => `${translationId}`;

class MainRouter extends React.Component {
  constructor(props) {
    super(props);

    this.props.initialize({
      languages: [
        { name: 'Arabic', code: 'ar' },
        { name: 'English', code: 'en' },
        { name: 'French', code: 'fr' },
        { name: 'German', code: 'de' },
        { name: 'Italian', code: 'it' },
        { name: 'Spanish', code: 'es' },
        { name: 'Finnish', code: 'fi' },
        { name: 'Portuguese', code: 'pt' },
      ],
      options: {
        renderToStaticMarkup,
        onMissingTranslation,
      },
    });

    this.props.addTranslationForLanguage(en, 'en');
    this.props.addTranslationForLanguage(fr, 'fr');
    this.props.addTranslationForLanguage(es, 'es');
    this.props.addTranslationForLanguage(en, 'pt');
    this.props.addTranslationForLanguage(en, 'it');
    this.props.addTranslationForLanguage(en, 'de');
    this.props.addTranslationForLanguage(ar, 'ar');
    this.props.addTranslationForLanguage(en, 'fi');
  }

  componentDidMount() {
    this.props.fetchSessionInfo();
    this.props.fetchTranslations('', 'default');
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale !== nextProps.locale) {
      this.props.setActiveLanguage(nextProps.locale);

      if (this.props.locale) {
        this.props.fetchTranslations(nextProps.locale, 'default');
      }
    }
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

export default withLocalize(connect(mapStateToProps, {
  fetchTranslations, fetchSessionInfo,
})(MainRouter));

MainRouter.propTypes = {
  initialize: PropTypes.func.isRequired,
  addTranslationForLanguage: PropTypes.func.isRequired,
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  setActiveLanguage: PropTypes.func.isRequired,
  /** Function called to get the currently selected location */
  fetchSessionInfo: PropTypes.func.isRequired,
};
