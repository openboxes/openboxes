import React from 'react';
import { setActiveLanguage, getLanguages } from 'react-localize-redux';
import { connect } from 'react-redux';
import _ from 'lodash';
import PropTypes from 'prop-types';
import Translate from '../../utils/Translate';
import { changeCurrentLocale } from '../../actions';


const Footer = ({
  // eslint-disable-next-line no-shadow
  setActiveLanguage, changeCurrentLocale, locale, languages,
  hostname, timezone, ipAddress, grailsVersion, appVersion,
  branchName, buildNumber, environment, buildDate,
}) => (
  <div className="border-top align-self-end text-center py-2 w-100 footer">
    <div className="d-flex flex-row justify-content-center m-2 flex-wrap">
      <div className="mx-3">© {(new Date().getFullYear())} <a href="https://openboxes.com"><Translate id="react.default.poweredBy.label " defaultMessage="Powered by OpenBoxes" /></a></div> {'|'}
      <div className="mx-3"><Translate id="react.default.grailsVersion.label " defaultMessage="Grails Version" />: <b>{grailsVersion}</b></div>{'|'}
      <div className="mx-3"><Translate id="react.default.version.label " defaultMessage="Application version" />: <b>{appVersion}</b></div> {'|'}
      <div className="mx-3"><Translate id="react.default.branch.label " defaultMessage="Branch" />: <b>{branchName}</b> </div> {'|'}
      <div className="mx-3"> <Translate id="react.default.buildNumber.label " defaultMessage="Build Number" />: <b>{buildNumber}</b> </div> {'|'}
      <div className="mx-3"><Translate id="react.default.environment.label " defaultMessage="Environment" />: <b>{environment}</b></div> {'|'}
      <div className="mx-3"><Translate id="react.default.buildDate.label " defaultMessage="Build Date" />: <b>{buildDate}</b></div>
    </div>
    <div className="d-flex flex-row justify-content-center mb-3 flex-wrap">
      <div className="mx-3"><Translate id="react.default.locale.label " defaultMessage="Locale" />: {' '}
        { _.map(languages, language => (
          <a
            className={`${locale === language.code ? 'selected' : ''}`}
            key={language.code}
            href="#"
            onClick={() => {
              changeCurrentLocale(language.code);
              setActiveLanguage(language.code);
            }
            }
          >
            <Translate id={`react.default.${language.name.toLowerCase()}.label`} /> {''}
          </a>
        ))
        }
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
});

export default connect(mapStateToProps, { changeCurrentLocale, setActiveLanguage })(Footer);

Footer.propTypes = {
  languages: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  setActiveLanguage: PropTypes.func.isRequired,
  /** Function called to change the currently selected location */
  changeCurrentLocale: PropTypes.func.isRequired,
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
};
