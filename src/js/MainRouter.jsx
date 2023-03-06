import React from 'react';

import PropTypes from 'prop-types';
import { renderToStaticMarkup } from 'react-dom/server';
import { withLocalize } from 'react-localize-redux';
import connect from 'react-redux/es/connect/connect';

import { fetchMenuConfig, fetchSessionInfo, fetchTranslations } from 'actions';
import Router from 'components/Router';

const onMissingTranslation = ({ translationId }) => `${translationId}`;

const TRANSLATION_PREFIXES = ['default', 'dashboard', 'combinedShipments', 'productsConfiguration',
  'locationsConfiguration', 'loadData', 'notification'];

// TODO: Refactor fetching app context (fetchSessionInfo)
// TODO: Refactor fetching menu config
// TODO: Refactor fetching localizations (react-localize-redux)

class MainRouter extends React.Component {
  componentDidMount() {
    this.props.fetchSessionInfo().then(() => {
      this.props.initialize({
        languages: this.props.supportedLocales,
        options: {
          renderToStaticMarkup,
          onMissingTranslation,
        },
      });
      this.props.setActiveLanguage(this.props.locale);
      this.props.fetchMenuConfig();
      TRANSLATION_PREFIXES.forEach(prefix => this.props.fetchTranslations('', prefix));
    });
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale !== nextProps.locale) {
      this.props.setActiveLanguage(nextProps.locale);

      if (this.props.locale) {
        TRANSLATION_PREFIXES.forEach(prefix => this.props.fetchTranslations('', prefix));
      }

      this.props.fetchMenuConfig();
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
  supportedLocales: state.session.supportedLocales,
});

export default withLocalize(connect(mapStateToProps, {
  fetchTranslations, fetchSessionInfo, fetchMenuConfig,
})(MainRouter));


MainRouter.propTypes = {
  initialize: PropTypes.func.isRequired,
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  setActiveLanguage: PropTypes.func.isRequired,
  /** Function called to get the currently selected location */
  fetchSessionInfo: PropTypes.func.isRequired,
  fetchMenuConfig: PropTypes.func.isRequired,
  supportedLocales: PropTypes.arrayOf(PropTypes.shape({
    code: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired).isRequired,
};
