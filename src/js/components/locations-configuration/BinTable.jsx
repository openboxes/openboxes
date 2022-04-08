import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import ReactTable from 'react-table';

import ModalWrapper from 'components/form-elements/ModalWrapper';
import apiClient from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

const INITIAL_STATE = {
  binPages: -1,
  binLoading: true,
};


class BinTable extends Component {
  constructor(props) {
    super(props);
    this.state = {
      ...INITIAL_STATE,
    };
  }

  render() {
    const binColumns = [
      {
        Header: 'Status',
        accessor: 'active',
        minWidth: 30,
        className: 'active-circle',
        headerClassName: 'header',
        Cell: (row) => {
          if (row.original.active) {
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
        accessor: 'locationType.name',
        className: 'cell',
        headerClassName: 'header text-align-left',
      },
      {
        Header: 'Zone location',
        accessor: 'zoneName',
        minWidth: 50,
        className: 'cell',
        headerClassName: 'header text-align-left',
      },
      {
        Header: 'Actions',
        minWidth: 20,
        accessor: 'actions',
        className: 'action-cell',
        headerClassName: 'header ',
        Cell: row => (
          <div className="d-flex justify-content-center align-items-center">
            <ModalWrapper
              onSave={values => this.props.handleLocationEdit(values)}
              fields={this.props.FIELDS}
              validate={this.props.validate}
              initialValues={
                {
                  ...row.original,
                  locationType: row.original.locationType,
                  zoneLocation: { id: row.original.zoneId, name: row.original.zoneName },
                }
              }
              formProps={{
                binTypes: this.props.binTypes,
                zoneData: this.props.zoneData,
              }}
              title="react.locationsConfiguration.editBin.label"
              defaultTitleMessage="Edit Bin Location"
              btnSaveDefaultText="Save"
              btnOpenAsIcon
              btnOpenIcon="fa-pencil"
              btnOpenClassName="action-icons icon-pointer"
              btnContainerClassName="d-flex justify-content-end"
              btnContainerStyle={{ gap: '3px' }}
              btnSaveClassName="btn btn-primary"
              btnCancelClassName="btn btn-outline-primary"
            >
              <div className="form-subtitle mb-lg-4">
                <Translate
                  id="react.locationsConfiguration.editBin.additionalTitle.label"
                  defaultMessage="Bin locations represent a physical storage location within a depot.
                  Bins are defined by a unique name or code that indicates the position within the depot.
                  Common bin names might include a pallet position number, rack number, or shelf action."
                />
              </div>
            </ModalWrapper>
            <i className="fa fa-trash-o action-icons icon-pointer" aria-hidden="true" onClick={() => this.props.deleteLocation(row.original)} />
          </div>
        ),
      },
    ];

    return (
      <ReactTable
        data={this.props.binData}
        ref={this.props.refBinTable}
        columns={binColumns}
        loading={this.state.binLoading}
        pages={this.state.binPages}
        defaultPageSize={10}
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
          apiClient.get('/openboxes/api/internalLocations/search?locationTypeCode=BIN_LOCATION&locationTypeCode=INTERNAL', {
            params: {
              offset: `${offset}`,
              max: `${state.pageSize}`,
              'parentLocation.id': `${this.props.currentLocationId}`,
              includeInactive: true,
            },
          })
            .then((res) => {
              this.setState({
                binLoading: false,
                binPages: Math.ceil(res.data.totalCount / state.pageSize),
              });
              this.props.updateBinData(res.data.data);
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
  binData: PropTypes.shape([]).isRequired,
  updateBinData: PropTypes.func.isRequired,
  handleLocationEdit: PropTypes.func.isRequired,
  deleteLocation: PropTypes.func.isRequired,
  FIELDS: PropTypes.shape({}).isRequired,
  validate: PropTypes.func.isRequired,
  binTypes: PropTypes.shape([]).isRequired,
  refBinTable: PropTypes.oneOfType([
    PropTypes.func,
    PropTypes.shape({ current: PropTypes.instanceOf(Element) }),
  ]).isRequired,
  zoneData: PropTypes.shape([]).isRequired,
};

export default connect(mapStateToProps)(BinTable);
