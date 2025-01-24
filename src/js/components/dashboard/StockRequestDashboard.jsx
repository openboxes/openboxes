import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { RiStickyNoteFill } from 'react-icons/ri';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import ReactTable from 'react-table';
import { Tooltip } from 'react-tippy';

import { fetchTranslations } from 'actions';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import apiClient from 'utils/apiClient';
import { getCurrentEventComment } from 'utils/list-utils';
import Translate from 'utils/Translate';

import 'react-table/react-table.css';
import './StockRequestDashboard.scss';

const COLUMNS = [
  {
    Header: 'Request Number',
    accessor: 'identifier',
    headerClassName: 'text-left font-weight-bold px-4 py-3',
    className: 'px-4 py-2 btn-link',
    Cell: (row) => (
      <a href={STOCK_MOVEMENT_URL.show(row.original.id)}>
        { row.original.identifier }
      </a>
    ),
  },
  {
    Header: 'Current Status',
    accessor: 'displayStatus',
    headerClassName: 'text-left font-weight-bold px-4 py-3',
    className: 'px-4 py-2',
    maxWidth: 200,
    Cell: (row) => {
      const comment = getCurrentEventComment(row.original.currentEvent, row.original.statusCode);
      return (
        <span className="d-flex align-items-center">
          <span className="d-inline-block text-overflow-ellipsis">
            {row.original.displayStatus?.label}
          </span>
          {comment && (
            <Tooltip
              html={comment}
              theme="transparent"
              delay="150"
              duration="250"
              hideDelay="50"
            >
              <RiStickyNoteFill className="text-warning mr-1" />
            </Tooltip>
          )}
        </span>
      );
    },
  },
  {
    Header: 'Description',
    accessor: 'description',
    headerClassName: 'text-left font-weight-bold px-4 py-3',
    className: 'px-4 py-2 btn-link',
    Cell: (row) => (
      <a href={STOCK_MOVEMENT_URL.show(row.original.id)}>
        { row.original.description }
      </a>
    ),
  },
  {
    Header: 'Fulfiling Depot',
    accessor: 'origin.name',
    headerClassName: 'text-left font-weight-bold px-4 py-3',
    className: 'px-4 py-2',
  },
  {
    Header: 'Requested By',
    accessor: 'requestedBy.name',
    headerClassName: 'text-left font-weight-bold px-4 py-3',
    className: 'px-4 py-2',
  },
  {
    Header: 'Date Requested',
    accessor: 'dateRequested',
    headerClassName: 'text-left font-weight-bold px-4 py-3',
    className: 'px-4 py-2',
    maxWidth: 180,
  },
];

class StockRequestDashboard extends Component {
  constructor(props) {
    super(props);
    this.state = {
      stockRequests: [],
      isLoading: true,
      pageCount: 0,
    };
    this.fetchStockMovementItems = this.fetchStockMovementItems.bind(this);
  }

  componentDidMount() {
    this.props.fetchTranslations('', 'stockMovement');
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.locale && this.props.locale !== nextProps.locale) {
      this.props.fetchTranslations(nextProps.locale, 'stockMovement');
    }
  }

  fetchStockMovementItems(page, pageSize) {
    const url = '/api/stockMovements';
    const params = {
      destination: this.props.currentLocation.id,
      direction: 'INBOUND',
      offset: page * pageSize,
      max: pageSize,
      sort: 'dateRequested',
    };

    this.setState({ isLoading: true });
    apiClient.get(url, { params })
      .then((response) => {
        const { data, totalCount } = response.data;
        this.setState({
          pageCount: Math.ceil(totalCount / pageSize),
          stockRequests: data,
        });
      }).finally(() => {
        this.setState({ isLoading: false });
      });
  }

  render() {
    return (
      <div className="p-3">
        <div className="d-flex justify-content-between my-3">
          <h3 className="font-weight-bold">
            <Translate
              id="react.stockMovement.stockRequests.label"
              defaultMessage="Stock Requests"
            />
          </h3>
          <Link
            to={STOCK_MOVEMENT_URL.createRequest()}
            className="btn btn-primary d-flex justify-content-center align-items-center font-weight-bold"
          >
            <i className="fa fa-plus mr-1" />
            <Translate
              id="react.stockMovement.createNewRequest.label"
              defaultMessage="Create New Request"
            />
          </Link>
        </div>
        <ReactTable
          data={this.state.stockRequests}
          columns={COLUMNS}
          defaultPageSize={10}
          loading={this.state.isLoading}
          sortable={false}
          pages={this.state.pageCount}
          manual
          resizable={false}
          className="-striped -highlight stock-request-table "
          previousText={<i className="fa fa-chevron-left" aria-hidden="true" />}
          nextText={<i className="fa fa-chevron-right" aria-hidden="true" />}
          pageText=""
          onFetchData={(tableState) => {
            this.fetchStockMovementItems(tableState.page, tableState.pageSize);
          }}
        />
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  locale: state.session.activeLanguage,
  currentLocation: state.session.currentLocation,
});

export default connect(mapStateToProps, { fetchTranslations })(StockRequestDashboard);

StockRequestDashboard.propTypes = {
  fetchTranslations: PropTypes.func.isRequired,
  locale: PropTypes.string.isRequired,
  currentLocation: PropTypes.shape({
    name: PropTypes.string,
    id: PropTypes.string,
    locationType: PropTypes.shape({
      description: PropTypes.string,
      locationTypeCode: PropTypes.string,
    }),
  }).isRequired,
};
