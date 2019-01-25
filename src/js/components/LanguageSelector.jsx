/* eslint-disable no-shadow */
import React from 'react';
import { setActiveLanguage, getLanguages, withLocalize } from 'react-localize-redux';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { connect } from 'react-redux';
import { changeCurrentLocale } from '../actions';

const LanguageSelector = ({ languages, setActiveLanguage, changeCurrentLocale }) => (
  <div className="dropdown language-selector mx-1">
    <div className="nav-item dropdown">
      <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" aria-haspopup="true" aria-expanded="false">
        {_.find(languages, language => language.active).name}
      </a>
      <div className="dropdown-menu language-selector-menu dropdown-menu-right" aria-labelledby="navbarDropdown">
        { _.map(languages, language => (
          <a
            className="dropdown-item"
            key={language.code}
            href="#"
            onClick={() => {
              changeCurrentLocale(language.code);
              setActiveLanguage(language.code);
            }
            }
          >
            {language.name}
          </a>
          ))
        }
      </div>
    </div>
  </div>
);

const mapStateToProps = state => ({
  languages: getLanguages(state.localize),
});
const mapDispatchToProps = { setActiveLanguage, getLanguages, changeCurrentLocale };

export default withLocalize(connect(mapStateToProps, mapDispatchToProps)(LanguageSelector));

LanguageSelector.propTypes = {
  languages: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  setActiveLanguage: PropTypes.func.isRequired,
  /** Function called to change the currently selected location */
  changeCurrentLocale: PropTypes.func.isRequired,
};
