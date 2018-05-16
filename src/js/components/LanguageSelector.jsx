/* eslint-disable no-shadow */
import React from 'react';
import { setActiveLanguage, getLanguages } from 'react-localize-redux';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { connect } from 'react-redux';

const LanguageSelector = ({ languages, setActiveLanguage }) => (
  <div className="dropdown language-selector mr-1">
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
            onClick={() => setActiveLanguage(language.code)}
          >
            {language.name}
          </a>
          ))
        }
      </div>
    </div>
  </div>
);

const mapStateToProps = state => ({ languages: getLanguages(state.locale) });
const mapDispatchToProps = { setActiveLanguage, getLanguages };

export default connect(mapStateToProps, mapDispatchToProps)(LanguageSelector);

LanguageSelector.propTypes = {
  languages: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  setActiveLanguage: PropTypes.func.isRequired,
};
