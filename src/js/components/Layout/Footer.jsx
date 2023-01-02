import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { getLanguages, setActiveLanguage } from 'react-localize-redux';
import { connect } from 'react-redux';

import { changeCurrentLocale } from 'actions';
import Translate from 'utils/Translate';


const Footer = ({
  // eslint-disable-next-line no-shadow
  setLanguage,
  changeLocale,
  locale,
  languages,
  hostname,
  timezone,
  ipAddress,
  grailsVersion,
  appVersion,
  branchName,
  buildNumber,
  environment,
  buildDate,
  localizationModeEnabled,
  localizationModeLocale,
}) => (
  <div className="border-top align-self-end text-center py-2 w-100 footer">
    <div className="d-flex flex-row justify-content-center m-2 flex-wrap">
      <div className="mx-3">© {(new Date().getFullYear())} <a href="https://openboxes.com"><Translate id="react.default.poweredBy.label" defaultMessage="Powered by OpenBoxes" /></a></div> {'|'}
      <div className="mx-3"><Translate id="react.default.grailsVersion.label " defaultMessage="Grails Version" />: <b>{grailsVersion}</b></div>{'|'}
      <div className="mx-3"><Translate id="react.default.version.label " defaultMessage="Application version" />: <b>{appVersion}</b></div> {'|'}
      <div className="mx-3"><Translate id="react.default.branch.label " defaultMessage="Branch" />: <b>{branchName}</b> </div> {'|'}
      <div className="mx-3"> <Translate id="react.default.buildNumber.label " defaultMessage="Build Number" />: <b>{buildNumber}</b> </div> {'|'}
      <div className="mx-3"><Translate id="react.default.environment.label " defaultMessage="Environment" />: <b>{environment}</b></div> {'|'}
      <div className="mx-3"><Translate id="react.default.buildDate.label " defaultMessage="Build Date" />: <b>{buildDate}</b></div>
    </div>
    <div className="d-flex flex-row justify-content-center mb-3 flex-wrap">
      <div className="mx-3"><Translate id="react.default.locale.label " defaultMessage="Locale" />: {' '}
        { _.map(languages, (language) => {
          // When clicking on language that is a translation mode language, enable localization mode
          if (language.code === localizationModeLocale) {
            return (
              <a
                className={`${locale === language.code ? 'selected' : ''}`}
                key={language.code}
                href="/openboxes/user/enableLocalizationMode"
              >
                <Translate id={`react.default.${language.name.toLowerCase()}.label`} />
              </a>
            );
          }
          // If we are in localization mode and we click on non-translation mode language,
          // we want to disable the localization mode
          if (localizationModeEnabled) {
            return (
              <a
                className={`${locale === language.code ? 'selected' : ''}`}
                key={language.code}
                href={`/openboxes/user/disableLocalizationMode?locale=${language.code}`}
              >
                <Translate id={`react.default.${language.name.toLowerCase()}.label`} />
              </a>
            );
          }
          // If we are not in localization mode and the language is not a translation mode language,
          // we just want to change the language
          return (
            <button
              className={`${locale === language.code ? 'selected' : ''}`}
              key={language.code}
              onClick={() => {
                changeLocale(language.code);
                setLanguage(language.code);
              }}
            >
              <Translate id={`react.default.${language.name.toLowerCase()}.label`} />
            </button>
          );
        })}
      </div> {'|'}

      <div className="mx-3"><Translate id="react.default.ipAddress.label " defaultMessage="IP Address" />: <b>{ipAddress}</b></div> {'|'}

      <div className="mx-3"> <Translate id="react.default.hostname.label " defaultMessage="Hostname" />: <b>{hostname}</b></div> {'|'}
      <div className="mx-3"><Translate id="react.default.timezone.label " defaultMessage="Timezone" />: <b>{timezone}</b></div> {'|'}
    </div>
  </div>
);

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  grailsVersion: state.session.grailsVersion,
  appVersion: state.session.appVersion,
  branchName: state.session.branchName,
  buildNumber: state.session.buildNumber,
  environment: state.session.environment,
  buildDate: state.session.buildDate,
  hostname: state.session.hostname,
  timezone: state.session.timezone,
  ipAddress: state.session.ipAddress,
  languages: getLanguages(state.localize),
  localizationModeEnabled: state.session.localizationModeEnabled,
  localizationModeLocale: state.session.localizationModeLocale,
});

const mapDispatchToProps = {
  changeLocale: changeCurrentLocale,
  setLanguage: setActiveLanguage,
};

export default connect(mapStateToProps, mapDispatchToProps)(Footer);

Footer.propTypes = {
  languages: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  setLanguage: PropTypes.func.isRequired,
  /** Function called to change the currently selected location */
  changeLocale: PropTypes.func.isRequired,
  locale: PropTypes.string.isRequired,
  grailsVersion: PropTypes.string.isRequired,
  appVersion: PropTypes.string.isRequired,
  branchName: PropTypes.string.isRequired,
  buildNumber: PropTypes.string.isRequired,
  environment: PropTypes.string.isRequired,
  buildDate: PropTypes.string.isRequired,
  hostname: PropTypes.string.isRequired,
  timezone: PropTypes.string.isRequired,
  ipAddress: PropTypes.string.isRequired,
  localizationModeEnabled: PropTypes.bool.isRequired,
  localizationModeLocale: PropTypes.string.isRequired,
};
