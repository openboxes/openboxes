import React, { useEffect, useRef, useState } from 'react';

import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

import { debounceProductsFetch } from 'utils/option-utils';
import renderHandlingIcons from 'utils/product-handling-icons';
import Select from 'utils/Select';

const Option = option => (
  <Tooltip
    html={<div className="text-truncate">{option.name}</div>}
    theme="transparent"
    disabled={!option.displayName}
    position="top-start"
  >
    <strong style={{ color: option.color || 'black' }} className="d-flex align-items-center">
      {option.label}
        &nbsp;
      {renderHandlingIcons(option.handlingIcons)}
    </strong>
  </Tooltip>);

const SelectedValue = option => (
  <span className="d-flex align-items-center">
    <span className="text-truncate">
      {option.label || `${option.productCode} - ${option.displayName || option.displayNames?.default || option.name}`}
    </span>
    {renderHandlingIcons(option?.handlingIcons)}
  </span>
);

const ProductSelect = ({
  onExactProductSelected, locationId, fieldRef, ...props
}) => {
  const selectRef = useRef(null);
  const [isExactMatch, setIsExactMatch] = useState(false);
  const [loadedOptions, setLoadedOptions] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const {
    debounceTime, minSearchLength,
  } = useSelector(state => ({
    debounceTime: state.session.searchConfig.debounceTime,
    minSearchLength: state.session.searchConfig.minSearchLength,
  }));

  const debouncedProductsFetch = debounceProductsFetch(
    debounceTime,
    minSearchLength,
    locationId,
  );

  const onEnterPress = (event) => {
    event.preventDefault();
    event.stopPropagation();
    setIsExactMatch(true);
  };

  useEffect(() => {
    if (isExactMatch && loadedOptions.length && searchTerm) {
      const exactMatches = loadedOptions.filter(product => product.exactMatch);
      let exactMatchProduct = null;

      if (exactMatches.length === 1) {
        [exactMatchProduct] = exactMatches;
      } else if (exactMatches.length > 1) {
        // if there are more than one exact match
        // then select one that matches productCode with search string
        const matchedByProductCode = exactMatches
          .find(({ productCode }) => productCode === searchTerm);
        if (matchedByProductCode) exactMatchProduct = matchedByProductCode;
      }

      if (onExactProductSelected) {
        onExactProductSelected({ product: exactMatchProduct });
      }
      if (exactMatchProduct) {
        selectRef.current.select.select.setValue(exactMatchProduct);
      }

      setIsExactMatch(false);
      setLoadedOptions([]);
    }
  }, [isExactMatch, loadedOptions, searchTerm]);

  const loadProductOptions = (searchString, callback) =>
    debouncedProductsFetch(searchString, (resultOptions) => {
      setLoadedOptions(resultOptions);
      setSearchTerm(searchString);
      callback(resultOptions);
    });

  return (
    <Select
      {...props}
      fieldRef={(el) => {
        selectRef.current = el;
        if (fieldRef) fieldRef(el);
      }}
      async
      options={[]}
      loadOptions={props.loadOptions || loadProductOptions}
      onMenuClose={() => {
        setLoadedOptions([]);
        setSearchTerm('');
      }}
      filterOption={item => (item)}
      onEnterPress={onEnterPress}
      optionRenderer={Option}
      valueRenderer={SelectedValue}
    />);
};

ProductSelect.defaultProps = {
  className: 'text-left',
  openOnClick: true,
  autoload: true,
  cache: false,
  showValueTooltip: true,
  onExactProductSelected: undefined,
  fieldRef: undefined,
  loadOptions: undefined,
};

ProductSelect.propTypes = {
  className: PropTypes.string,
  openOnClick: PropTypes.bool,
  autoload: PropTypes.bool,
  cache: PropTypes.bool,
  showValueTooltip: PropTypes.bool,
  locationId: PropTypes.string.isRequired,
  onExactProductSelected: PropTypes.func,
  fieldRef: PropTypes.func,
  loadOptions: PropTypes.func,
};

export default ProductSelect;
