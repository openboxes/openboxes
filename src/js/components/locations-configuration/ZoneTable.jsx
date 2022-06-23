import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import ReactTable from 'react-table';

import ModalWrapper from 'components/form-elements/ModalWrapper';
import apiClient from 'utils/apiClient';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'components/locations-configuration/ZoneTable.scss';

const INITIAL_STATE = {
  zonePages: -1,
  zoneLoading: true,
};


class ZoneTable extends Component {
  constructor(props) {
    super(props);
    this.state = {
      ...INITIAL_STATE,
    };
  }

  render() {
    const zoneColumns = [
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
        Header: 'Location Type',
        accessor: 'locationType.locationTypeCode',
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
                }
              }
              formProps={{
                zoneTypes: this.props.zoneTypes,
              }}
              title="react.locationsConfiguration.editZone.label"
              defaultTitleMessage="Edit Zone Location"
              btnSaveDefaultText="Save"
              btnOpenAsIcon
              btnOpenIcon="fa-pencil"
              btnOpenClassName="action-icons"
              btnContainerClassName="d-flex justify-content-end"
              btnContainerStyle={{ gap: '3px' }}
              btnSaveClassName="btn btn-primary"
              btnCancelClassName="btn btn-outline-primary"
            >
              <div className="form-subtitle mb-lg-4">
                <Translate
                  id="react.locationsConfiguration.addZone.additionalTitle.label"
                  defaultMessage="Zones are large areas within a depot encompassing multiple bin locations.
                                    They may represent different rooms or buildings within a depot space.
                                    To remove a zone from your depot, uncheck the box to mark it as inactive."
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
        data={this.props.zoneData}
        ref={this.props.refZoneTable}
        columns={zoneColumns}
        loading={this.state.zoneLoading}
        pages={this.state.zonePages}
        defaultPageSize={5}
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
              includeInactive: true,
            },
          })
            .then((res) => {
              this.setState({
                zoneLoading: false,
                zonePages: Math.ceil(res.data.totalCount / state.pageSize),
              });
              this.props.updateZoneData(res.data.data);
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
  zoneData: PropTypes.shape([]).isRequired,
  updateZoneData: PropTypes.func.isRequired,
  handleLocationEdit: PropTypes.func.isRequired,
  deleteLocation: PropTypes.func.isRequired,
  FIELDS: PropTypes.shape({}).isRequired,
  validate: PropTypes.func.isRequired,
  zoneTypes: PropTypes.shape([]).isRequired,
  refZoneTable: PropTypes.oneOfType([
    PropTypes.func,
    PropTypes.shape({ current: PropTypes.instanceOf(Element) }),
  ]).isRequired,
};

export default connect(mapStateToProps)(ZoneTable);
