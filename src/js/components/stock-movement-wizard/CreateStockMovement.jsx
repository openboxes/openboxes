import React, { Component } from 'react';
import { connect } from 'react-redux';
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
import { showSpinner, hideSpinner, fetchLocations, fetchUsers } from '../../actions';

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
      stockLists: [],
    };
    this.fetchStockLists = this.fetchStockLists.bind(this);
  }

  componentDidMount() {
    if (!this.props.usersFetched) {
      this.fetchData(this.props.fetchUsers);
    }
    if (!this.props.locationsFetched) {
      this.fetchData(this.props.fetchLocations);
    }
  }

  fetchData(fetchFunction) {
    this.props.showSpinner();
    fetchFunction()
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  fetchStockLists(destination) {
    this.props.showSpinner();
    const url = `/openboxes/api/stocklists?destination.id=${destination.id}`;

    return apiClient.get(url)
      .then((response) => {
        const stockLists = _.map(response.data.data, stockList => (
          { value: stockList.id, label: stockList.name }
        ));
        this.setState({ stockLists }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <form onSubmit={this.props.handleSubmit}>
        {_.map(
          FIELDS,
          (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName, {
            users: this.props.users,
            locations: this.props.locations,
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

const mapStateToProps = state => ({
  locationsFetched: state.locations.fetched,
  locations: state.locations.data,
  usersFetched: state.users.fetched,
  users: state.users.data,
});

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
  validate,
})(connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchLocations, fetchUsers,
})(CreateStockMovement));

CreateStockMovement.propTypes = {
  handleSubmit: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  fetchLocations: PropTypes.func.isRequired,
  locationsFetched: PropTypes.bool.isRequired,
  locations: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  fetchUsers: PropTypes.func.isRequired,
  usersFetched: PropTypes.bool.isRequired,
  users: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};
