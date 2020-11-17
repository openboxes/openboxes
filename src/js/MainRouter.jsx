import React from 'react';
import PropTypes from 'prop-types';
import { renderToStaticMarkup } from 'react-dom/server';
import { withLocalize } from 'react-localize-redux';
import connect from 'react-redux/es/connect/connect';

import Router from './components/Router';

import { fetchTranslations, fetchSessionInfo, fetchMenuConfig } from './actions';

const onMissingTranslation = ({ translationId }) => `${translationId}`;

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
      this.props.fetchTranslations('', 'default');
      this.props.fetchTranslations('', 'dashboard');
      this.props.fetchTranslations('', 'combinedShipments');
    });
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale !== nextProps.locale) {
      this.props.setActiveLanguage(nextProps.locale);

      if (this.props.locale) {
        this.props.fetchMenuConfig();
        this.props.fetchTranslations(nextProps.locale, 'default');
        this.props.fetchTranslations(nextProps.locale, 'dashboard');
        this.props.fetchTranslations('', 'combinedShipments');
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
