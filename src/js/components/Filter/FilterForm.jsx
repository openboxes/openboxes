import React, { useState } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';

import FilterVisibilityToggler from 'components/Filter/FilterVisibilityToggler';
import Button from 'components/form-elements/Button';
import SearchField from 'components/form-elements/SearchField';
import { renderFormField } from 'utils/form-utils';

import 'components/Filter/FilterStyles.scss';


const FilterForm = ({
  filterFields,
  onSubmit,
  searchFieldPlaceholder,
  searchFieldId,
  formProps,
  defaultValues,
  allowEmptySubmit,
  hidden,
}) => {
  const [amountFilled, setAmountFilled] = useState(0);
  const [filtersHidden, setFiltersHidden] = useState(hidden);

  const searchField = {
    type: SearchField,
    attributes: {
      placeholder: searchFieldPlaceholder,
      filterElement: true,
    },
  };
  // Create initialValues from filterFields as empty values
  let initialValues = Object.keys(filterFields)
    .reduce((acc, key) => ({ ...acc, [`${key}`]: null }), {});
  initialValues = { ...initialValues, ...defaultValues };

  // Calculate which object's values are not empty
  const countFilled = (values) => {
    setAmountFilled(Object.values(values)
      .filter((value) => {
        if (typeof value === 'object') return !_.isEmpty(value);
        return !!value;
      }).length);
  };

  return (
    <div className="filter-form">
      <Form
        onSubmit={onSubmit}
        initialValues={initialValues}
        render={({ values, handleSubmit, form }) => {
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
                      onClick={() => form.reset(initialValues)}
                      variant="transparent"
                      type="submit"
                    />
                    <Button
                      defaultLabel="Search"
                      label="react.button.search.label"
                      disabled={!allowEmptySubmit && _.every(values, value => !value)}
                      variant="primary"
                      type="submit"
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

export default FilterForm;


FilterForm.propTypes = {
  filterFields: PropTypes.shape({}).isRequired,
  onSubmit: PropTypes.func.isRequired,
  searchFieldPlaceholder: PropTypes.string,
  formProps: PropTypes.shape({}),
  searchFieldId: PropTypes.string,
  defaultValues: PropTypes.shape({}),
  allowEmptySubmit: PropTypes.bool,
  hidden: PropTypes.bool,
};

FilterForm.defaultProps = {
  searchFieldPlaceholder: 'Search',
  searchFieldId: 'searchTerm',
  formProps: {},
  defaultValues: {},
  allowEmptySubmit: false,
  hidden: true,
};
