import React, { useEffect, useRef, useState } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { setShouldRebuildFilterParams } from 'actions';
import FilterVisibilityToggler from 'components/Filter/FilterVisibilityToggler';
import Button from 'components/form-elements/Button';
import SearchField from 'components/form-elements/SearchField';
import useTranslation from 'hooks/useTranslation';
import { renderFormField } from 'utils/form-utils';
import { translateWithDefaultMessage } from 'utils/Translate';

import 'components/Filter/FilterStyles.scss';


const FilterForm = ({
  filterFields,
  updateFilterParams,
  searchFieldPlaceholder,
  searchFieldDefaultPlaceholder,
  searchFieldId,
  formProps,
  defaultValues,
  allowEmptySubmit,
  hidden,
  onClear,
  ignoreClearFilters,
  currentLocation,
  translate,
  setShouldRebuildFilterValues,
}) => {
  const [amountFilled, setAmountFilled] = useState(0);
  const [filtersHidden, setFiltersHidden] = useState(hidden);
  const formRef = useRef(null);

  const submitOnEnter = (event) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      if (event.target.value) {
        formRef.current.submit();
      }
    }
  };

  const searchField = {
    type: SearchField,
    attributes: {
      placeholder: translate(searchFieldPlaceholder, searchFieldDefaultPlaceholder),
      filterElement: true,
      onKeyPress: submitOnEnter,
    },
  };

  // Default values can change based on currentLocation
  // or any async data defaultValues are waiting for
  useEffect(() => {
    updateFilterParams({ ...defaultValues });
  }, [defaultValues]);

  useTranslation('button');

  // Calculate which object's values are not empty
  const countFilled = (values) => {
    setAmountFilled(Object.entries(values)
      .filter(([key, value]) => {
        // Ignore accounting for filter that is disabled
        const dynamicAttributes = _.invoke(filterFields, `${key}.getDynamicAttr`, formProps);
        const attributes = _.get(filterFields, `${key}.attributes`);
        if (dynamicAttributes?.disabled || attributes?.disabled) return false;

        // Ignore filter that is not specified in filterFields config
        // and that is not a search field
        if (!filterFields[key] && key !== searchFieldId) return false;
        // evaluate filter value
        if (typeof value === 'object') return !_.isEmpty(value);
        return !!value;
      }).length);
  };

  const onClearHandler = (form) => {
    if (onClear && typeof onClear === 'function') {
      onClear(form);
      return;
    }
    const clearedFilterList = Object.keys(defaultValues)
      .reduce((acc, key) => {
        if (ignoreClearFilters.includes(key)) {
          return { ...acc, [key]: defaultValues[key] };
        }
        return { ...acc, [key]: '' };
      }, {});
    setShouldRebuildFilterValues(true);
    form.reset(clearedFilterList);
  };

  useEffect(() => {
    if (formRef.current) {
      onClearHandler(formRef.current);
    }
  }, [currentLocation?.id]);

  return (
    <div className="filter-form">
      <Form
        onSubmit={updateFilterParams}
        initialValues={{ ...defaultValues }}
        render={({ values, handleSubmit, form }) => {
          formRef.current = form;
          countFilled(values);
          return (
            <form onSubmit={handleSubmit} className="w-100 m-0">
              <div className="classic-form with-description align-items-center flex-wrap">
                <div className="w-100 d-flex filter-header align-items-center">
                  <div className="min-w-50 d-flex align-items-center gap-8">
                    {renderFormField(searchField, searchFieldId)}
                    <FilterVisibilityToggler
                      amountFilled={amountFilled}
                      filtersHidden={filtersHidden}
                      setFiltersHidden={setFiltersHidden}
                    />
                  </div>
                  <div className="d-flex justify-content-end buttons">
                    <Button
                      defaultLabel="Clear"
                      label="react.button.clear.label"
                      onClick={() => onClearHandler(form)}
                      variant="transparent"
                      type="button"
                    />
                    <Button
                      defaultLabel="Search"
                      label="react.button.search.label"
                      onClick={() => updateFilterParams(values)}
                      disabled={!allowEmptySubmit && _.every(values, value => !value)}
                      variant="primary"
                      type="button"
                    />
                  </div>
                </div>

                <div className="d-flex pt-2 flex-wrap gap-8 align-items-center filters-row">
                  {!filtersHidden && _.map(filterFields, (fieldConfig, fieldName) =>
                    renderFormField(fieldConfig, fieldName, formProps))}
                </div>
              </div>
            </form>
          );
        }
        }
      />
    </div>
  );
};

const mapStateToProps = state => ({
  currentLocation: state.session.currentLocation,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

const mapDispatchToProps = {
  setShouldRebuildFilterValues: setShouldRebuildFilterParams,
};

export default connect(mapStateToProps, mapDispatchToProps)(FilterForm);


FilterForm.propTypes = {
  filterFields: PropTypes.shape({}).isRequired,
  updateFilterParams: PropTypes.func.isRequired,
  onClear: PropTypes.func,
  searchFieldPlaceholder: PropTypes.string.isRequired,
  searchFieldDefaultPlaceholder: PropTypes.string,
  formProps: PropTypes.shape({}),
  searchFieldId: PropTypes.string,
  defaultValues: PropTypes.shape({}),
  allowEmptySubmit: PropTypes.bool,
  hidden: PropTypes.bool,
  ignoreClearFilters: PropTypes.arrayOf(PropTypes.string),
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
  }).isRequired,
  translate: PropTypes.func.isRequired,
  setShouldRebuildFilterValues: PropTypes.func.isRequired,
};

FilterForm.defaultProps = {
  searchFieldDefaultPlaceholder: 'Search',
  searchFieldId: 'searchTerm',
  formProps: {},
  defaultValues: {},
  allowEmptySubmit: false,
  hidden: true,
  onClear: undefined,
  ignoreClearFilters: [],
};
