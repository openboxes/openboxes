import React, { useState } from 'react';

import PropTypes from 'prop-types';
import { RiCloseLine, RiSearchLine } from 'react-icons/ri';
import { connect } from 'react-redux';
import { Async, components } from 'react-select';

import { debounceGlobalSearch } from 'utils/option-utils';

import 'components/GlobalSearch/GlobalSearch.scss';

// eslint-disable-next-line react/prop-types
const ValueContainer = ({ children, ...props }) => (
  <React.Fragment>
    <RiSearchLine className="app-global-search__search-icon" />
    <components.ValueContainer {...props}>
      {children}
    </components.ValueContainer>
  </React.Fragment>
);

const GlobalSearch = ({ renderButton, debounceTime, minSearchLength }) => {
  const [isVisible, setIsVisible] = useState(false);
  const searchTerm = debounceGlobalSearch(debounceTime, minSearchLength);

  const showSearchbar = () => setIsVisible(true);

  const hideSearchbar = () => setIsVisible(false);


  const onKeyPressHandler = (event) => {
    if (event.key === 'Enter') {
      window.location = `/openboxes/dashboard/globalSearch?searchTerms=${event.target.value}`;
    }
  };

  const onOptionSelectedHandler = (data) => {
    window.location = data.url;
  };

  const splitMatchingStr = (data, str) => {
    const indexOfMatched = data.indexOf(str);
    if (indexOfMatched < 0) {
      return { before: data };
    }
    const before = data.slice(0, indexOfMatched);
    const matched = data.slice(indexOfMatched, indexOfMatched + str.length);
    const after = data.slice(indexOfMatched + str.length, data.length);

    return { before, matched, after };
  };

  const Option = (props) => {
    // eslint-disable-next-line react/prop-types
    const { selectProps, data } = props;
    const { inputValue } = selectProps;
    const { before, matched, after } = splitMatchingStr(data.label, inputValue);
    return (
      <components.Option {...props}>
        <div style={{ color: data.color }}>
          {before && <span>{before}</span>}
          {matched && <strong className="font-weight-bold">{matched}</strong>}
          {after && <span>{after}</span>}
        </div>
      </components.Option>
    );
  };


  const DropdownIndicator = props => (
    <components.IndicatorsContainer {...props}>
      <button className="app-global-search__clear-btn" onClick={hideSearchbar}>
        <RiCloseLine />
      </button>
    </components.IndicatorsContainer>
  );


  return (
    <div className="position-relative d-flex">
      {renderButton({ isVisible, showSearchbar })}
      {isVisible && (
        <Async
          className="app-global-search"
          classNamePrefix="app-global-search"
          autoFocus
          openMenuOnClick={false}
          loadOptions={searchTerm}
          onKeyDown={onKeyPressHandler}
          onChange={onOptionSelectedHandler}
          components={{
            ValueContainer,
            DropdownIndicator,
            Option,
          }}
        />)}
    </div>);
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
});

export default connect(mapStateToProps)(GlobalSearch);

GlobalSearch.propTypes = {
  renderButton: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
};

