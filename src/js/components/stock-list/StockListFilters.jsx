import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';

import FilterForm from 'components/Filter/FilterForm';
import CheckboxField from 'components/form-elements/CheckboxField';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import apiClient from 'utils/apiClient';

const filterFields = {
  origin: {
    type: FilterSelectField,
    attributes: {
      className: 'location-select',
      multi: true,
      filterElement: true,
      placeholder: 'Origin',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
      valueKey: 'id',
      labelKey: 'name',
    },
    getDynamicAttr: ({ locations }) => ({
      options: locations,
    }),
  },
  destination: {
    type: FilterSelectField,
    attributes: {
      className: 'location-select',
      multi: true,
      filterElement: true,
      placeholder: 'Destination',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
      valueKey: 'id',
      labelKey: 'name',
    },
    getDynamicAttr: ({ locations }) => ({
      options: locations,
    }),
  },
  isPublished: {
    type: CheckboxField,
    label: 'react.stocklists.includeUnpublished.label',
    defaultMessage: 'Include unpublished stocklists',
    attributes: {
      filterElement: true,
    },
  },
};

const StockListFilters = ({ setFilterParams }) => {
  const [locations, setLocations] = useState([]);

  useEffect(() => {
    apiClient.get('/openboxes/api/locations')
      .then((response) => {
        const { data } = response.data;
        setLocations(data);
      });
  }, []);


  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        searchFieldId="q"
        filterFields={filterFields}
        updateFilterParams={values => setFilterParams({ ...values })}
        formProps={{ locations }}
        allowEmptySubmit
        searchFieldPlaceholder="Search by stocklist name"
        hidden={false}
      />
    </div>
  );
};

export default StockListFilters;

StockListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
};
