import React, { Component } from 'react';
import { defaults } from 'react-chartjs-2';
import { connect } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import 'react-table/react-table.css';
import { numberData } from '../../../assets/dataFormat/numberData';
import { addToIndicators, fetchIndicators, reorderIndicators } from '../../actions';
import apiClient from '../../utils/apiClient';
import GraphCard from './GraphCard';
import NumberCard from './NumberCard';
import './tablero.scss';
import UnarchiveIndicator from './UnarchivePopout';

// Disable charts legends by default.
defaults.global.legend = false;

const SortableCards = SortableContainer(({ data }) => (
  <div className="cardComponent">
    {data.map((value, index) =>
      (value.archived ? null : (
        <GraphCard
          key={`item-${value.id}`}
          index={index}
          cardTitle={value.title}
          cardType={value.type}
          data={value.data}
        />
      )))}
  </div>
));

const NumberCardsRow = ({ data }) => (
  <div className="cardComponent">
    {data.map((value, index) => (
      <NumberCard
        key={`item-${value.id}`}
        index={index}
        cardTitle={value.title}
        cardNumber={value.number}
        cardSubtitle={value.subtitle}
      />
    ))}
  </div>
);

const ArchiveIndicator = ({ hideArchive }) => (
  <div className={hideArchive ? 'archiveDiv hideArchive' : 'archiveDiv'}>
    <span>
      Archive indicator <i className="fa fa-archive" />
    </span>
  </div>
);

class Tablero extends Component {
  state = {
    isDragging: false,
    showPopout: false,
  };
  componentDidMount() {
    this.fetchData();
  }
  dataFetched = false;

  fetchData() {
    this.props.fetchIndicators();
    this.fetchNumbersData();
  }

  fetchNumbersData() {
    const url = "/openboxes/apitablero/getNumberData";
    apiClient.get(url).then(res => {
      this.setState({ numberData: res.data });
    });
  }

  sortStartHandle = () => {
    this.setState({ isDragging: true });
  };

  sortEndHandle = ({ oldIndex, newIndex }, e) => {
    const maxHeight = window.innerHeight - (((6 * window.innerHeight) / 100) + 80);
    if (e.clientY > maxHeight) {
      e.target.id = 'archive';
    }
    this.props.reorderIndicators({ oldIndex, newIndex }, e);
    this.setState({ isDragging: false });
  };

  unarchiveHandler = () => {
    this.props.indicatorsData.filter(data => data.archived).length
      ? this.setState({ showPopout: !this.state.showPopout })
      : this.setState({ showPopout: false });
  };

  handleAdd = (index) => {
    this.props.addToIndicators(index);
    this.props.indicatorsData.filter(data => data.archived).length
      ? this.setState({ showPopout: true })
      : this.setState({ showPopout: false });
  };

  render() {
    return (
      <div className="cardsContainer">
        <NumberCardsRow data={numberData} />
        <SortableCards
          data={this.props.indicatorsData}
          onSortStart={this.sortStartHandle}
          onSortEnd={this.sortEndHandle}
          axis="xy"
          useDragHandle
        />
        <ArchiveIndicator hideArchive={!this.state.isDragging} />
        <UnarchiveIndicator
          data={this.props.indicatorsData}
          showPopout={this.state.showPopout}
          unarchiveHandler={this.unarchiveHandler}
          handleAdd={this.handleAdd}
        />
      </div>
    );
  }
}

const mapStateToProps = state => ({
  indicatorsData: state.indicators.data
});

export default connect(mapStateToProps, {
  fetchIndicators,
  addToIndicators,
  reorderIndicators,
})(Tablero);
