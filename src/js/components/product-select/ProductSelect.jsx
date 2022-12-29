import React, { useEffect, useRef, useState } from 'react';

import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';

import { debounceProductsFetch } from 'utils/option-utils';
import renderHandlingIcons from 'utils/product-handling-icons';
import Select from 'utils/Select';

const Option = option => (
  <strong style={{ color: option.color || 'black' }} className="d-flex align-items-center">
    {option.label}
  &nbsp;
    {renderHandlingIcons(option.handlingIcons)}
  </strong>);

const SelectedValue = option => (
  <span className="d-flex align-items-center">
    <span className="text-truncate">{option.label}</span>
    &nbsp;{renderHandlingIcons(option ? option.handlingIcons : [])}
  </span>
);

const ProductSelect = ({
  onExactProductSelected, locationId, fieldRef, ...props
}) => {
  const selectRef = useRef(null);
  const [isExactMatch, setIsExactMatch] = useState(false);
  const [loadedOptions, setLoadedOptions] = useState([]);
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
    if (isExactMatch && loadedOptions.length > 0) {
      const exactMatchProduct = loadedOptions.find(product => product.exactMatch);

      if (onExactProductSelected) {
        onExactProductSelected({ isExactMatch, product: exactMatchProduct });
      }
      if (exactMatchProduct) {
        selectRef.current.select.select.setValue(exactMatchProduct);
      }

      setIsExactMatch(false);
      setLoadedOptions([]);
    }
  }, [isExactMatch, loadedOptions]);

  const loadProductOptions = (searchString, callback) =>
    debouncedProductsFetch(searchString, (resultOptions) => {
      setLoadedOptions(resultOptions);
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
      loadOptions={loadProductOptions}
      onMenuClose={() => setLoadedOptions([])}
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
};

export default ProductSelect;
