import React, { useState } from 'react';

import PropTypes from 'prop-types';
import { RiCloseLine, RiSearchLine } from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { Async, components } from 'react-select';
import { Tooltip } from 'react-tippy';

import { debounceGlobalSearch } from 'utils/option-utils';
import { translateWithDefaultMessage } from 'utils/Translate';

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

const GlobalSearch = ({
  className, visible, renderButton, debounceTime, minSearchLength, translate,
}) => {
  const [isVisible, setIsVisible] = useState(visible);

  const searchItems = debounceGlobalSearch(debounceTime, minSearchLength);

  const showSearchbar = () => setIsVisible(true);

  const hideSearchbar = () => setIsVisible(false);

  const hideSearchbarOnBlur = () => {
    if (renderButton) {
      hideSearchbar();
    }
  };

  const onKeyPressHandler = (event) => {
    if (event.key === 'Enter') {
      window.location = `/openboxes/dashboard/globalSearch?searchTerms=${event.target.value}`;
    }
  };

  const onOptionSelectedHandler = (data) => {
    if (data) {
      window.location = data.url;
    }
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
        <Tooltip
          html={<div className="custom-tooltip">{data.originalName}</div>}
          theme="transparent"
          disabled={!data?.displayName}
          position="top-start"
        >
          <div style={{ color: data.color }}>
            {before && <span>{before}</span>}
            {matched && <strong className="font-weight-bold">{matched}</strong>}
            {after && <span>{after}</span>}
          </div>
        </Tooltip>
      </components.Option>
    );
  };


  const DropdownIndicator = (props) => {
    const clearOrHide = () => {
      // eslint-disable-next-line react/prop-types
      const { hasValue, setValue } = props;
      if (hasValue) {
        setValue('');
        return;
      }
      if (renderButton) {
        hideSearchbar();
      }
    };
    return (
      <components.IndicatorsContainer {...props}>
        <button
          className="app-global-search__clear-btn"
          onClick={clearOrHide}
        >
          <RiCloseLine />
        </button>
      </components.IndicatorsContainer>
    );
  };


  return (
    <div className="position-relative d-flex">
      {renderButton?.({ isVisible, showSearchbar })}
      {isVisible && (
        <Async
          className={`app-global-search ${renderButton ? 'position-absolute' : ''} ${className}`}
          classNamePrefix="app-global-search"
          autoFocus
          openMenuOnClick={false}
          loadingMessage={() => translate('react.default.loading.label', 'Loading...')}
          noOptionsMessage={() => translate('react.default.noOptions.label', 'No options')}
          loadOptions={searchItems}
          onKeyDown={onKeyPressHandler}
          onChange={onOptionSelectedHandler}
          onBlur={hideSearchbarOnBlur}
          placeholder={translate('react.default.globalSearch.placeholder.label', 'Search...')}
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
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps)(GlobalSearch);

GlobalSearch.propTypes = {
  visible: PropTypes.bool,
  renderButton: PropTypes.func,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  className: PropTypes.string,
  translate: PropTypes.func.isRequired,
};

GlobalSearch.defaultProps = {
  renderButton: undefined,
  visible: false,
  className: '',
};
