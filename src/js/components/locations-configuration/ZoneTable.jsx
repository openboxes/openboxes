import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import ReactTable from 'react-table';

import apiClient from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

const INITIAL_STATE = {
  zoneData: [],
  zonePages: -1,
  zoneLoading: true,
};

const zoneColumns = [
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
    Header: 'Location Type',
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


class ZoneTable extends Component {
  constructor(props) {
    super(props);
    this.state = {
      ...INITIAL_STATE,
    };
  }

  render() {
    return (
      <ReactTable
        data={this.state.zoneData}
        columns={zoneColumns}
        loading={this.state.zoneLoading}
        pages={this.state.zonePages}
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
              locationTypeCode: 'ZONE',
              offset: `${offset}`,
              max: `${state.pageSize}`,
              'parentLocation.id': `${this.props.currentLocationId}`,
            },
          })
            .then((res) => {
              this.setState({
                zoneData: res.data.data,
                zoneLoading: false,
                zonePages: Math.ceil(res.data.totalCount / state.pageSize),
              });
            })
            .catch(() => Promise.reject(new Error(this.props.translate('react.locationsConfiguration.error.zoneList.label', 'Could not get list of zones'))));
        }}
      />
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

ZoneTable.propTypes = {
  currentLocationId: PropTypes.string.isRequired,
  translate: PropTypes.func.isRequired,
};

export default connect(mapStateToProps)(ZoneTable);
