import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import ReactTable from 'react-table';

import apiClient from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

const INITIAL_STATE = {
  binData: [],
  binPages: -1,
  binLoading: true,
};

const binColumns = [
  {
    Header: 'Status',
    accessor: 'active',
    minWidth: 30,
    className: 'active-circle',
    headerClassName: 'header',
    Cell: (row) => {
      if (row) {
        return (<i className="fa fa-check-circle green-circle" aria-hidden="true" />);
      }
      return (<i className="fa fa-times-circle grey-circle" aria-hidden="true" />);
    },
  },
  {
    Header: 'Name',
    accessor: 'name',
    className: 'cell',
    headerClassName: 'header text-align-left',
  },
  {
    Header: 'Bin Type',
    accessor: 'locationType.locationTypeCode',
    className: 'cell',
    headerClassName: 'header text-align-left',
  },
  {
    Header: 'Actions',
    minWidth: 20,
    accessor: 'actions',
    className: 'cell',
    headerClassName: 'header ',
    Cell: () => (
      <div className="d-flex justify-content-center align-items-center">
        <i className="fa fa-pencil action-icons" aria-hidden="true" />
        <i className="fa fa-trash-o action-icons" aria-hidden="true" />
      </div>
    ),
  },
];


class BinTable extends Component {
  constructor(props) {
    super(props);
    this.state = {
      ...INITIAL_STATE,
    };
  }

  render() {
    return (
      <ReactTable
        data={this.state.binData}
        columns={binColumns}
        loading={this.state.binLoading}
        pages={this.state.binPages}
        manual
        className="-striped -highlight zoneTable"
        resizable={false}
        sortable={false}
        multiSort={false}
        previousText={<i className="fa fa-chevron-left" aria-hidden="true" />}
        nextText={<i className="fa fa-chevron-right" aria-hidden="true" />}
        pageText=""
        onFetchData={(state) => {
          const offset = state.page > 0 ? (state.page) * state.pageSize : 0;
          apiClient.get('/openboxes/api/internalLocations/search', {
            params: {
              locationTypeCode: 'BIN_LOCATION',
              offset: `${offset}`,
              max: `${state.pageSize}`,
              // it needs to be further changed - there shouldn't be parentLocation.id there as the
              // session.currentLocation, but the location we are currently creating
              'parentLocation.id': `${this.props.currentLocationId}`,
            },
          })
            .then((res) => {
              this.setState({
                binData: res.data.data,
                binLoading: false,
                binPages: Math.ceil(res.data.totalCount / state.pageSize),
              });
            })
            .catch(() => Promise.reject(new Error(this.props.translate('react.locationsConfiguration.error.binList.label', 'Could not get list of bin locations'))));
        }}
      />
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

BinTable.propTypes = {
  currentLocationId: PropTypes.string.isRequired,
  translate: PropTypes.func.isRequired,
};

export default connect(mapStateToProps)(BinTable);
