import React, { Component } from 'react';
import { reduxForm } from 'redux-form';
import PropTypes from 'prop-types';
import _ from 'lodash';

import { validate } from './validate';
import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import DateField from '../form-elements/DateField';
import ValueSelectorField from '../form-elements/ValueSelectorField';
import { renderFormField } from '../../utils/form-utils';
import apiClient from '../../utils/apiClient';

const FIELDS = {
  description: {
    type: TextField,
    label: 'Description',
    attributes: {
      required: true,
    },
  },
  origin: {
    type: SelectField,
    label: 'Origin',
    attributes: {
      required: true,
      objectValue: true,
    },
    getDynamicAttr: props => ({
      options: props.locations,
    }),
  },
  destination: {
    type: SelectField,
    label: 'Destination',
    attributes: {
      required: true,
      objectValue: true,
    },
    getDynamicAttr: props => ({
      onChange: (value) => {
        if (value) {
          props.fetchStockLists(value);
        }
      },
      options: props.locations,
    }),
  },
  stockList: {
    label: 'Stock list',
    type: ValueSelectorField,
    component: SelectField,
    componentConfig: {
      getDynamicAttr: ({ selectedValue, stockLists }) => ({
        disabled: !selectedValue,
        options: stockLists,
      }),
    },
    attributes: {
      formName: 'stock-movement-wizard',
    },
    getDynamicAttr: () => ({
      field: 'origin',
    }),
  },
  requestedBy: {
    type: SelectField,
    label: 'Requested-by',
    attributes: {
      required: true,
    },
    getDynamicAttr: props => ({
      options: props.users,
    }),
  },
  dateRequested: {
    type: DateField,
    label: 'Date requested',
    attributes: {
      required: true,
      dateFormat: 'DD/MMM/YYYY',
    },
  },
};

class CreateStockMovement extends Component {
  constructor(props) {
    super(props);
    this.state = {
      locations: [],
      users: [],
      stockLists: [],
    };
    this.fetchStockLists = this.fetchStockLists.bind(this);
  }

  componentDidMount() {
    this.fetchUsers();
    this.fetchLocations();
  }

  fetchUsers() {
    const url = '/openboxes/api/generic/person';

    return apiClient.get(url)
      .then((response) => {
        const users = _.map(response.data.data, user => (
          { value: user.id, label: user.name }
        ));
        this.setState({ users });
      });
  }

  fetchStockLists(destination) {
    const url = `/openboxes/api/stocklists?destination.id=${destination.id}`;

    return apiClient.get(url)
      .then((response) => {
        const stockLists = _.map(response.data.data, stockList => (
          { value: stockList.id, label: stockList.name }
        ));
        this.setState({ stockLists });
      });
  }

  fetchLocations() {
    const url = '/openboxes/api/locations';

    return apiClient.get(url)
      .then((response) => {
        const locations = _.map(response.data.data, location => (
          {
            value: { id: location.id, type: location.locationTypeCode, name: location.name },
            label: `${location.name} [${location.locationTypeCode}]`,
          }
        ));
        this.setState({ locations });
      });
  }

  render() {
    return (
      <form onSubmit={this.props.handleSubmit}>
        {_.map(
          FIELDS,
          (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
            users: this.state.users,
            locations: this.state.locations,
            stockLists: this.state.stockLists,
            fetchStockLists: this.fetchStockLists,
          }),
        )}
        <div className="row col-md-6">
          <button type="submit" className="btn btn-outline-primary text-right">
          Next
          </button>
        </div>
      </form>
    );
  }
}

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(CreateStockMovement);

CreateStockMovement.propTypes = {
  handleSubmit: PropTypes.func.isRequired,
};
