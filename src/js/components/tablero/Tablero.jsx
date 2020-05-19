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
} from '../../actions';
import GraphCard from './GraphCard';
import LoadingNumbers from './LoadingNumbers';
import NumberCard from './NumberCard';
import UnarchiveIndicators from './UnarchiveIndicators';
import './tablero.scss';

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
          cardDataTooltip={value.listTooltipData}
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


class Tablero extends Component {
  constructor(props) {
    super(props);

    this.state = {
      isDragging: false,
      showPopout: false,
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

  fetchData() {
    this.props.resetIndicators();
    if (this.props.dashboardConfig && this.props.dashboardConfig.endpoints) {
      this.props.fetchIndicators(this.props.dashboardConfig);
    } else {
      this.props.fetchConfigAndData();
    }
  }

  loadIndicator = (id, params) => {
    const indicatorConfig = Object.values(this.props.dashboardConfig.endpoints.graph)
      .filter(config => config.order === id)[0];

    this.props.reloadIndicator(indicatorConfig, params);
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
    this.setState({ isDragging: false });
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
    if (size) this.setState({ showPopout: true });
    else this.setState({ showPopout: false });
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
    );
  }
}

const mapStateToProps = state => ({
  indicatorsData: state.indicators.data,
  numberData: state.indicators.numberData,
  dashboardConfig: state.indicators.config,
  currentLocation: state.session.currentLocation.id,
});

export default connect(mapStateToProps, {
  fetchIndicators,
  reloadIndicator,
  addToIndicators,
  reorderIndicators,
  resetIndicators,
  fetchConfigAndData,
})(Tablero);

Tablero.defaultProps = {
  currentLocation: '',
  indicatorsData: null,
  numberData: [],
};

Tablero.propTypes = {
  fetchIndicators: PropTypes.func.isRequired,
  reorderIndicators: PropTypes.func.isRequired,
  indicatorsData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  numberData: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  dashboardConfig: PropTypes.shape({
    enabled: PropTypes.bool,
    endpoints: PropTypes.shape({
      graph: PropTypes.shape({}),
      number: PropTypes.shape({}),
    }),
  }).isRequired,
  currentLocation: PropTypes.string.isRequired,
  addToIndicators: PropTypes.func.isRequired,
  reloadIndicator: PropTypes.func.isRequired,
  resetIndicators: PropTypes.func.isRequired,
  fetchConfigAndData: PropTypes.func.isRequired,
};

ArchiveIndicator.propTypes = {
  hideArchive: PropTypes.bool.isRequired,
};
