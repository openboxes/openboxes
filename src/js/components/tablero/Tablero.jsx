import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { defaults } from 'react-chartjs-2';
import { connect } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import 'react-table/react-table.css';
import {
  addToIndicators,
  fetchIndicators,
  reorderIndicators,
  reloadIndicator,
  resetIndicators,
  fetchConfigAndData,
  fetchConfig,
} from '../../actions';
import GraphCard from './GraphCard';
import LoadingNumbers from './LoadingNumbers';
import NumberCard from './NumberCard';
import UnarchiveIndicators from './UnarchiveIndicators';
import './tablero.scss';
import apiClient from '../../utils/apiClient';

// Disable charts legends by default.
defaults.global.legend = false;
defaults.scale.ticks.beginAtZero = true;


// eslint-disable-next-line no-shadow
const SortableCards = SortableContainer(({ data, loadIndicator }) => (
  <div className="card-component">
    {data.map((value, index) =>
      (value.archived ? null : (
        <GraphCard
          key={`item-${value.id}`}
          index={index}
          cardId={value.id}
          cardTitle={value.title}
          cardType={value.type}
          cardLink={value.link}
          data={value.data}
          filter={value.filter}
          options={value.options}
          loadIndicator={loadIndicator}
        />
      )))}
  </div>
));


const SortableNumberCards = SortableContainer(({ data }) => (
  <div className="card-component">
    {data.map((value, index) => (
      (value.archived ? null : (
        <NumberCard
          key={`item-${value.id}`}
          index={index}
          cardTitle={value.title}
          cardNumber={value.number}
          cardSubtitle={value.subtitle}
          cardLink={value.link}
          cardDataTooltip={value.tooltipData}
        />
      ))
    ))}
  </div>
));


const ArchiveIndicator = ({ hideArchive }) => (
  <div className={hideArchive ? 'archive-div hide-archive' : 'archive-div'}>
    <span>
      Archive indicator <i className="fa fa-archive" />
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
              {value}
            </button>
          </li>
          ))}
      </ul>
      {
        (activeConfig === 'personal' && configModified) ?
          <div className="update-section">
            <div className="division-line" />
            <span> <i className="fa fa-info-circle" aria-hidden="true" />The dashboard layout has been edited</span>
            <button onClick={updateConfig} >
              <i className="fa fa-floppy-o" aria-hidden="true" />
              Save configuration
            </button>
          </div>
        : null
      }
    </div>
  );
};


class Tablero extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isDragging: false,
      showPopout: false,
      showNav: false,
      configModified: false,
    };
  }

  componentDidMount() {
    this.fetchData();
  }

  componentDidUpdate(prevProps) {
    const prevLocation = prevProps.currentLocation;
    const newLocation = this.props.currentLocation;
    if (prevLocation !== '' && prevLocation !== newLocation) {
      this.fetchData();
    }
  }
  dataFetched = false;

  fetchData = (config = 'personal') => {
    this.props.resetIndicators();
    if (this.props.dashboardConfig && this.props.dashboardConfig.endpoints) {
      this.props.fetchIndicators(this.props.dashboardConfig, config);
    } else {
      this.props.fetchConfigAndData();
    }
  }

  updateConfig = () => {
    const url = '/openboxes/apitablero/updateConfig';

    const payload = {
      number: {},
      graph: {},
    };

    const configData = this.props.dashboardConfig.endpoints;
    Object.keys(configData.graph).forEach((key) => {
      const index = this.props.indicatorsData.findIndex(data => data &&
        data.id === configData.graph[key].order);
      payload.graph[key] = {
        order: index + 1,
        archived: this.props.indicatorsData[index].archived,
      };
    });

    Object.keys(configData.number).forEach((key) => {
      const index = this.props.numberData.findIndex(data => data &&
        data.id === configData.number[key].order);
      payload.number[key] = {
        order: index + 1,
        archived: this.props.numberData[index].archived,
      };
    });

    apiClient.post(url, payload).then(() => {
      this.props.fetchConfig();
      this.setState({ configModified: false });
    });
  }

  loadIndicator = (id, params) => {
    const indicatorConfig = Object.values(this.props.dashboardConfig.endpoints.graph)
      .filter(config => config.order === id)[0];

    this.props.reloadIndicator(indicatorConfig, params);
  }

  toggleNav = () => {
    this.setState({ showNav: !this.state.showNav });
  }

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
    const size = this.props.indicatorsData.filter(data => data.archived).length
      + this.props.numberData.filter(data => data.archived).length;
    if (size) this.setState({ showPopout: !this.state.showPopout });
    else this.setState({ showPopout: false });
  };

  handleAdd = (index, type) => {
    this.props.addToIndicators(index, type);

    const size = (this.props.indicatorsData.filter(data => data.archived).length
       + this.props.numberData.filter(data => data.archived).length) - 1;

    if (this.props.activeConfig === 'personal') {
      this.setState({
        configModified: true,
        showPopout: (size > 0),
      });
    } else {
      this.setState({ showPopout: (size > 0) });
    }
  };

  render() {
    let numberCards;
    if (this.props.numberData.length) {
      numberCards = (
        <SortableNumberCards
          data={this.props.numberData}
          onSortStart={this.sortStartHandle}
          onSortEnd={this.sortEndHandleNumber}
          axis="xy"
          useDragHandle
        />
      );
    } else {
      numberCards = <LoadingNumbers />;
    }

    return (
      <div className="dashboard-container">
        <ConfigurationsList
          configs={this.props.dashboardConfig.configurations || {}}
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
        <div className="cards-container">
          {numberCards}
          <SortableCards
            data={this.props.indicatorsData.filter(indicator => indicator)}
            onSortStart={this.sortStartHandle}
            onSortEnd={this.sortEndHandleGraph}
            loadIndicator={this.loadIndicator}
            axis="xy"
            useDragHandle
          />
          <ArchiveIndicator hideArchive={!this.state.isDragging} />
          <UnarchiveIndicators
            graphData={this.props.indicatorsData}
            numberData={this.props.numberData}
            showPopout={this.state.showPopout}
            unarchiveHandler={this.unarchiveHandler}
            handleAdd={this.handleAdd}
          />
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
});

export default connect(mapStateToProps, {
  fetchIndicators,
  reloadIndicator,
  addToIndicators,
  reorderIndicators,
  resetIndicators,
  fetchConfigAndData,
  fetchConfig,
})(Tablero);

Tablero.defaultProps = {
  currentLocation: '',
  indicatorsData: null,
  numberData: [],
  configModified: false,
};

Tablero.propTypes = {
  fetchIndicators: PropTypes.func.isRequired,
  reorderIndicators: PropTypes.func.isRequired,
  indicatorsData: PropTypes.arrayOf(PropTypes.shape({
    archived: PropTypes.oneOfType([PropTypes.bool, PropTypes.number]),
    id: PropTypes.number,
  })).isRequired,
  numberData: PropTypes.arrayOf(PropTypes.shape({
    archived: PropTypes.oneOfType([PropTypes.bool, PropTypes.number]),
    id: PropTypes.number,
  })).isRequired,
  dashboardConfig: PropTypes.shape({
    enabled: PropTypes.bool,
    configurations: PropTypes.shape({}),
    endpoints: PropTypes.shape({
      graph: PropTypes.shape({}),
      number: PropTypes.shape({}),
    }),
  }).isRequired,
  activeConfig: PropTypes.string.isRequired,
  currentLocation: PropTypes.string.isRequired,
  addToIndicators: PropTypes.func.isRequired,
  reloadIndicator: PropTypes.func.isRequired,
  resetIndicators: PropTypes.func.isRequired,
  fetchConfigAndData: PropTypes.func.isRequired,
  fetchConfig: PropTypes.func.isRequired,
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
