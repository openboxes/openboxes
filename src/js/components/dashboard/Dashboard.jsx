import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { defaults } from 'react-chartjs-2';
import { connect } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';

import {
  addToIndicators,
  fetchConfig,
  fetchConfigAndData,
  fetchIndicators,
  reloadIndicator,
  reorderIndicators,
  resetIndicators,
} from 'actions';
import Filter from 'components/dashboard/Filter';
import GraphCard from 'components/dashboard/GraphCard';
import LoadingNumbers from 'components/dashboard/LoadingNumbers';
import NumberCard from 'components/dashboard/NumberCard';
import UnarchiveIndicators from 'components/dashboard/UnarchiveIndicators';
import apiClient from 'utils/apiClient';
import Translate from 'utils/Translate';

import 'react-table/react-table.css';
import 'react-confirm-alert/src/react-confirm-alert.css';
import 'components/dashboard/Dashboard.scss';
// Disable charts legends by default.
defaults.scale.ticks.beginAtZero = true;

// TODO: OBPIH-4385 Refactor/Split this file into separate components

// eslint-disable-next-line no-shadow
const SortableCards = SortableContainer(({
  data, loadIndicator, allLocations, personalDashboardActive,
}) => (
  <div className="card-component">
    {data.map((value, index) =>
      (value &&
        <GraphCard
          key={`item-${value.id}`}
          index={index}
          cardId={value.id}
          widgetId={value.widgetId}
          cardTitle={value.title}
          cardType={value.type}
          cardLink={value.link}
          cardInfo={value.info}
          data={value.data}
          timeFilter={value.timeFilter}
          timeLimit={value.timeLimit}
          locationFilter={value.locationFilter}
          yearTypeFilter={value.yearTypeFilter}
          options={value.options}
          loadIndicator={loadIndicator}
          allLocations={allLocations}
          size={value.size}
          disabled={!personalDashboardActive}
          hideDraghandle={!personalDashboardActive}
        />
      ))}
  </div>
));


const SortableNumberCards = SortableContainer(({ data, personalDashboardActive }) => (
  <div className="card-component">
    {data.map((value, index) => (
      (value &&
        <NumberCard
          key={`item-${value.id}`}
          index={index}
          widgetId={value.widgetId}
          cardTitle={value.title}
          cardNumberType={value.numberType}
          cardNumber={value.number}
          cardSubtitle={value.subtitle}
          cardLink={value.link}
          cardDataTooltip={value.tooltipData}
          cardInfo={value.info}
          sparklineData={value.data}
          disabled={!personalDashboardActive}
          hideDraghandle={!personalDashboardActive}
        />
      )
    ))}
  </div>
));


const ArchiveIndicator = ({ hideArchive }) => (
  <div className={hideArchive ? 'archive-div hide-archive' : 'archive-div'}>
    <span>
      <Translate
        id="react.dashboard.archive.label"
        defaultMessage="Archive indicator"
      />
      <i className="fa fa-archive ml-2" />
    </span>
  </div>
);


const ConfigurationsList = ({
  configs, activeConfig, loadConfigData, showNav, toggleNav, configModified, updateConfig,
}) => {
  if (!configs) {
    return null;
  }
  return (
    <div className={`configs-left-nav ${!showNav ? 'hidden' : ''}`}>
      <button className="toggle-nav" onClick={toggleNav}>
        {showNav ?
          <i className="fa fa-chevron-left" aria-hidden="true" />
        :
          <i className="fa fa-chevron-right" aria-hidden="true" />
        }
      </button>
      <ul className="configs-list">
        {Object.entries(configs).map(([key, value]) => (
          <li className={`configs-list-item ${activeConfig === key ? 'active' : ''}`} key={key}>
            <button onClick={() => loadConfigData(key)}>
              <i className="fa fa-bar-chart" aria-hidden="true" />
              <Translate id={`react.dashboard.${key}.label`} defaultMessage={value.name} />
            </button>
          </li>
          ))}
      </ul>
      {
        (activeConfig === 'personal' && configModified) ?
          <div className="update-section">
            <div className="division-line" />
            <span>
              <i className="fa fa-info-circle" aria-hidden="true" />
              <Translate
                id="react.dashboard.hasBeenEdited.message"
                defaultMessage="The dashboard layout has been edited"
              />
            </span>
            <button onClick={updateConfig} >
              <i className="fa fa-floppy-o" aria-hidden="true" />
              <Translate
                id="react.dashboard.saveConfiguration.label"
                defaultMessage="Save configuration"
              />
            </button>
          </div>
        : null
      }
    </div>
  );
};

const MAIN_DASHBOARD_CONFIG = 'mainDashboard';


class Dashboard extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isDragging: false,
      showPopout: false,
      // Not showing if screen too small
      showNav: window.innerWidth > 1115,
      configModified: false,
      allLocations: [],
      pageFilters: [],
      subdashboardKeys: [],
    };
    this.fetchLocations();
  }

  componentDidMount() {
    if (this.props.currentLocation !== '') {
      this.getSubdashboardKeys().then(() => this.fetchData(this.determineActiveConfig()));
    }
    this.loadPageFilters(this.props.activeConfig);
  }

  componentDidUpdate(prevProps) {
    const prevLocation = prevProps.currentLocation;
    const newLocation = this.props.currentLocation;
    if (prevLocation !== newLocation) {
      this.getSubdashboardKeys().then(() => this.fetchData(this.determineActiveConfig()));
    }
    if (prevProps.dashboardConfig.dashboard !== this.props.dashboardConfig.dashboard) {
      this.loadPageFilters(this.props.activeConfig);
    }
  }

  getDashboardIdFromParams() {
    const dashboardId = this.props.match.params.configId;
    if (dashboardId && dashboardId !== 'index') {
      return dashboardId;
    }
    return MAIN_DASHBOARD_CONFIG;
  }

  getSubdashboardKeys() {
    const dashboardId = this.getDashboardIdFromParams();
    const url = `/openboxes/api/dashboard/${dashboardId}/subdashboardKeys`;
    return apiClient.get(url)
      .then((res) => {
        const subdashboardKeys = res.data;
        if (subdashboardKeys) {
          this.setState({ subdashboardKeys });
        }
      });
  }

  determineActiveConfig() {
    if (this.props.match.params.configId && this.props.match.params.configId !== 'index') {
      return this.props.match.params.configId;
    }
    const configFromSessionStorage = sessionStorage.getItem('dashboardKey');
    // eslint-disable-next-line max-len
    if (configFromSessionStorage && this.state.subdashboardKeys.includes(configFromSessionStorage)) {
      return configFromSessionStorage;
    }
    return 'personal';
  }

  dataFetched = false;

  loadPageFilters(config = '') {
    let pageFilters = [];
    if (this.props.dashboardConfig.dashboard) {
      const allPages = Object.entries(this.props.dashboardConfig.dashboard)
        .map(([key, value]) => [key, value]);
      allPages.forEach((page) => {
        const filters = Object.entries(page[1].filters)
          .map(([keyFilter, valueFilter]) => {
            const filter = {
              name: keyFilter,
              endpoint: valueFilter.endpoint,
            };
            return filter;
          });
        if (filters.length > 0 && page[0] === config) {
          pageFilters = filters;
        }
      });
    }
    this.setState({ pageFilters });
  }

  fetchLocations() {
    const url = '/openboxes/api/dashboard/fillRateDestinations';

    return apiClient.get(url)
      .then((response) => {
        this.setState({
          allLocations: response.data.data
            .sort((a, b) => a.id.localeCompare(b.id)),
        });
      });
  }

  fetchData = (config = 'personal') => {
    sessionStorage.setItem('dashboardKey', config);
    this.props.resetIndicators();
    if (this.props.dashboardConfig && this.props.dashboardConfig.dashboard) {
      this.props.fetchIndicators(
        this.props.dashboardConfig,
        config,
        this.props.currentLocation,
        this.props.currentUser,
      );
      this.loadPageFilters(config);
    } else {
      this.props.fetchConfigAndData(
        this.props.currentLocation,
        config,
        this.props.currentUser,
        // Determine which dashboard (id) to fetch
        this.getDashboardIdFromParams(),
      );
    }
  };

  updateConfig = () => {
    const widgets = [];

    _.forEach(this.props.indicatorsData, (widgetData, index) => {
      widgets.push({ widgetId: widgetData.widgetId, order: index + 1 });
    });

    _.forEach(this.props.numberData, (widgetData, index) => {
      widgets.push({ widgetId: widgetData.widgetId, order: index + 1 });
    });

    const url = '/openboxes/api/dashboard/config';
    const payload = {
      ...this.props.dashboardConfig.dashboard,
      [this.props.activeConfig]: {
        ...this.props.dashboardConfig.dashboard[this.props.activeConfig],
        widgets,
      },
    };

    apiClient.post(url, payload).then(() => {
      this.props.fetchConfig(this.getDashboardIdFromParams());
      this.setState({ configModified: false });
    });
  };

  loadIndicator = (widgetId, params) => {
    const dashboardConf = this.props.dashboardConfig.dashboard[this.props.activeConfig];
    const widget = _.find(dashboardConf.widgets, w => w.widgetId === widgetId);
    const widgetConf = {
      ...this.props.dashboardConfig.dashboardWidgets[widgetId],
      ...widget,
    };
    this.props.reloadIndicator(widgetConf, params, this.props.currentLocation);
  };

  toggleNav = () => {
    this.setState({ showNav: !this.state.showNav });
  };

  sortStartHandle = () => {
    this.setState({ isDragging: true });
  };

  sortEndHandle = ({ oldIndex, newIndex }, e, type) => {
    const maxHeight = window.innerHeight - (((6 * window.innerHeight) / 100) + 80);
    if (e.clientY > maxHeight) {
      e.target.id = 'archive';
    }
    this.props.reorderIndicators({ oldIndex, newIndex }, e, type);
    if (this.props.activeConfig === 'personal' && (oldIndex !== newIndex || e.target.id === 'archive')) {
      this.setState({
        configModified: true,
        isDragging: false,
      });
    } else {
      this.setState({ isDragging: false });
    }
  };

  sortEndHandleNumber = ({ oldIndex, newIndex }, e) => {
    this.sortEndHandle({ oldIndex, newIndex }, e, 'number');
  };

  sortEndHandleGraph = ({ oldIndex, newIndex }, e) => {
    this.sortEndHandle({ oldIndex, newIndex }, e, 'graph');
  };

  unarchiveHandler = () => {
    const size = _.size(this.props.dashboardConfig.dashboardWidgets)
      - (this.props.indicatorsData.length + this.props.numberData.length);

    if (size) {
      this.setState({ showPopout: !this.state.showPopout });
    } else {
      this.setState({ showPopout: false });
    }
  };

  handleAdd = (widgetId) => {
    const widget = this.props.dashboardConfig.dashboardWidgets[widgetId];
    const widgetConf = {
      ...widget,
      widgetId,
      order: (widget.type === 'number' ? this.props.numberData.length : this.props.indicatorsData.length) + 1,
    };
    this.props.addToIndicators(widgetConf, this.props.currentLocation, this.props.currentUser);

    const size = _.size(this.props.dashboardConfig.dashboardWidgets)
      - (this.props.indicatorsData.length + this.props.numberData.length);

    if (this.props.activeConfig === 'personal') {
      this.setState({
        configModified: true,
        showPopout: (size > 0),
      });
    } else {
      this.setState({ showPopout: (size > 0) });
    }
  };

  isPersonalDashboardActive() {
    return this.props.activeConfig === 'personal';
  }

  render() {
    const isPersonalDashboardActive = this.isPersonalDashboardActive();
    let numberCards;
    if (this.props.numberData.length) {
      numberCards = (
        <SortableNumberCards
          data={this.props.numberData}
          onSortStart={this.sortStartHandle}
          onSortEnd={this.sortEndHandleNumber}
          axis="xy"
          useDragHandle
          personalDashboardActive={isPersonalDashboardActive}
        />
      );
    } else {
      numberCards = <LoadingNumbers />;
    }

    return (
      <div className="dashboard-container">
        <ConfigurationsList
          configs={this.props.dashboardConfig.dashboard || {}}
          loadConfigData={this.fetchData}
          activeConfig={this.props.activeConfig}
          showNav={this.state.showNav}
          toggleNav={this.toggleNav}
          configModified={this.state.configModified}
          updateConfig={this.updateConfig}
        />
        <div
          className={`overlay ${this.state.showNav ? 'visible' : ''}`}
          role="button"
          tabIndex={0}
          onClick={this.toggleNav}
          onKeyPress={this.toggleNav}
        />

        <div className="filter-and-cards-container">
          <Filter
            configs={this.props.dashboardConfig.dashboard || {}}
            activeConfig={this.props.activeConfig}
            fetchData={this.fetchData}
            pageFilters={this.state.pageFilters}
          />
          <div className="cards-container">
            {numberCards}
            <SortableCards
              allLocations={this.state.allLocations}
              data={this.props.indicatorsData.filter(indicator => indicator)}
              onSortStart={this.sortStartHandle}
              onSortEnd={this.sortEndHandleGraph}
              loadIndicator={this.loadIndicator}
              axis="xy"
              useDragHandle
              personalDashboardActive={isPersonalDashboardActive}
            />
            <ArchiveIndicator hideArchive={!this.state.isDragging} />
            {isPersonalDashboardActive &&
              <UnarchiveIndicators
                graphData={this.props.indicatorsData}
                numberData={this.props.numberData}
                dashboardConfig={this.props.dashboardConfig}
                activeConfig={this.props.activeConfig}
                showPopout={this.state.showPopout}
                unarchiveHandler={this.unarchiveHandler}
                handleAdd={this.handleAdd}
              />
            }
          </div>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  indicatorsData: state.indicators.data,
  numberData: state.indicators.numberData,
  dashboardConfig: state.indicators.config,
  activeConfig: state.indicators.activeConfig,
  currentLocation: state.session.currentLocation.id,
  currentUser: state.session.user.id,
});

export default connect(mapStateToProps, {
  fetchIndicators,
  reloadIndicator,
  addToIndicators,
  reorderIndicators,
  resetIndicators,
  fetchConfigAndData,
  fetchConfig,
})(Dashboard);

Dashboard.defaultProps = {
  currentLocation: '',
  currentUser: '',
  indicatorsData: null,
  numberData: [],
  configModified: false,
  match: {
    params: { configId: 'personal' },
  },
};

Dashboard.propTypes = {
  fetchIndicators: PropTypes.func.isRequired,
  reorderIndicators: PropTypes.func.isRequired,
  indicatorsData: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.number,
  })).isRequired,
  numberData: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.number,
  })).isRequired,
  dashboardConfig: PropTypes.shape({
    dashboard: PropTypes.shape({}),
    dashboardWidgets: PropTypes.shape({}),
  }).isRequired,
  activeConfig: PropTypes.string.isRequired,
  currentLocation: PropTypes.string.isRequired,
  currentUser: PropTypes.string.isRequired,
  addToIndicators: PropTypes.func.isRequired,
  reloadIndicator: PropTypes.func.isRequired,
  resetIndicators: PropTypes.func.isRequired,
  fetchConfigAndData: PropTypes.func.isRequired,
  fetchConfig: PropTypes.func.isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({ configId: PropTypes.string }),
  }),
};

ArchiveIndicator.propTypes = {
  hideArchive: PropTypes.bool.isRequired,
};

ConfigurationsList.propTypes = {
  configs: PropTypes.shape({}).isRequired,
  activeConfig: PropTypes.string.isRequired,
  loadConfigData: PropTypes.func.isRequired,
  showNav: PropTypes.bool.isRequired,
  toggleNav: PropTypes.func.isRequired,
  updateConfig: PropTypes.func.isRequired,
  configModified: PropTypes.bool.isRequired,
};
