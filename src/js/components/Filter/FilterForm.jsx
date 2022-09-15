import React, { useState } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';

import FilterVisibilityToggler from 'components/Filter/FilterVisibilityToggler';
import ButtonPrimary from 'components/form-elements/ButtonPrimary';
import ButtonTransparent from 'components/form-elements/ButtonTransparent';
import TextField from 'components/form-elements/TextField';
import { renderFormField } from 'utils/form-utils';
import Translate from 'utils/Translate';

import 'components/Filter/FilterStyles.scss';


const FilterForm = ({
  filterFields, onSubmit, searchFieldPlaceholder, formProps,
}) => {
  // Replace with SearchField created in another ticket
  const searchField = {
    type: TextField,
    attributes: {
      placeholder: searchFieldPlaceholder,
    },
  };
  // Create initialValues from filterFields as empty values
  const initialValues = Object.keys(filterFields).reduce((acc, curr) => {
    if (!acc[curr]) {
      return {
        ...acc,
        [curr]: '',
      };
    }
    return acc;
  }, { name: '' });
  const [amountFilled, setAmountFilled] = useState(0);
  const countFilled = (values) => {
    // Calculate which object's values are not empty
    setAmountFilled(Object.keys(values).filter(key => values[key]).length);
  };
  const [filtersHidden, setFiltersHidden] = useState(true);


  return (
    <div className="filter-form">
      <Form
        onSubmit={onSubmit}
        initialValues={initialValues}
        render={({ values, handleSubmit, form }) => {
          countFilled(values);
          return (
            <form onSubmit={handleSubmit} className="w-100">
              <div className="classic-form align-items-center flex-wrap">
                <div className="w-100 d-flex filter-header">
                  <div className="d-flex w-50">
                    {renderFormField(searchField, 'name')}
                    <FilterVisibilityToggler
                      amountFilled={amountFilled}
                      filtersHidden={filtersHidden}
                      setFiltersHidden={setFiltersHidden}
                    />
                  </div>
                  <div className="d-flex justify-content-end buttons">
                    <ButtonTransparent
                      defaultLabel="Clear"
                      label="react.button.clear.label"
                      onClickAction={() => form.reset(initialValues)}
                    />
                    <ButtonPrimary
                      defaultLabel="Search"
                      label="react.button.search.label"
                      disabled={_.every(values, value => !value)}
                    />
                  </div>
                </div>

                <div className="d-flex">
                  {!filtersHidden && _.map(filterFields, (fieldConfig, fieldName) =>
                    renderFormField(fieldConfig, fieldName, formProps))}
                </div>

              </div>
              <div className="submit-buttons">
                <button
                  type="button"
                  onClick={() => onSubmit(values)}
                  className="btn btn-outline-primary float-left btn-xs"
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
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
};

FilterForm.defaultProps = {
  searchFieldPlaceholder: 'Search',
  formProps: {},
};
