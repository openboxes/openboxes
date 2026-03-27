import React, { useCallback, useState } from 'react';

import PropTypes from 'prop-types';
import { RiCloseLine, RiSearchLine } from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { components } from 'react-select';
import AsyncSelect from 'react-select/async';

import { GLOBAL_SEARCH } from 'api/urls';
import { debounceGlobalSearch } from 'utils/option-utils';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/GlobalSearch/GlobalSearch.scss';

const ValueContainer = ({ children, ...props }) => (
  <>
    <RiSearchLine className="app-global-search__search-icon" />
    <components.ValueContainer {...props}>
      {children}
    </components.ValueContainer>
  </>
);

const splitMatchingStr = (data, str) => {
  const indexOfMatched = data.toLowerCase().indexOf(str.toLowerCase());
  if (indexOfMatched < 0) {
    return { before: data };
  }
  const before = data.slice(0, indexOfMatched);
  const matched = data.slice(indexOfMatched, indexOfMatched + str.length);
  const after = data.slice(indexOfMatched + str.length, data.length);

  return { before, matched, after };
};

const Option = (props) => {
  const { selectProps, data } = props;
  const { inputValue } = selectProps;
  const { before, matched, after } = splitMatchingStr(data.label, inputValue);

  return (
    <components.Option {...props}>
      <div title={data?.displayName ? data.originalName : ''} style={{ color: data.color }}>
        {before && <span>{before}</span>}
        {matched && <strong className="font-weight-bold">{matched}</strong>}
        {after && <span>{after}</span>}
      </div>
    </components.Option>
  );
};

const DropdownIndicator = (props) => {
  const { hasValue, setValue, selectProps } = props;

  const clearOrHide = () => {
    if (hasValue) {
      setValue('');
      return;
    }
    if (selectProps.renderButton) {
      selectProps.hideSearchbar();
    }
  };

  return (
    <components.IndicatorsContainer {...props}>
      <button
        type="button"
        className="app-global-search__clear-btn"
        onClick={clearOrHide}
      >
        <RiCloseLine />
      </button>
    </components.IndicatorsContainer>
  );
};

const GlobalSearch = ({
  className, visible, renderButton, debounceTime, minSearchLength, translate,
}) => {
  const [isVisible, setIsVisible] = useState(visible);

  const searchItems = useCallback(
    debounceGlobalSearch(debounceTime, minSearchLength), [debounceTime, minSearchLength],
  );

  const showSearchbar = () => setIsVisible(true);
  const hideSearchbar = () => setIsVisible(false);

  const hideSearchbarOnBlur = () => {
    if (renderButton) {
      hideSearchbar();
    }
  };

  const onKeyPressHandler = (event) => {
    if (event.key === 'Enter') {
      window.location = GLOBAL_SEARCH(event.target.value);
    }
  };

  const onOptionSelectedHandler = (data) => {
    if (data) {
      window.location = data.url;
    }
  };

  return (
    <div className="position-relative d-flex">
      {renderButton?.({ isVisible, showSearchbar })}
      {isVisible && (
        <AsyncSelect
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
          renderButton={renderButton}
          hideSearchbar={hideSearchbar}
        />
      )}
    </div>
  );
};

const mapStateToProps = (state) => ({
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
