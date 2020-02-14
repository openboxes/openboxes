import React, { Component } from 'react';
import { defaults } from 'react-chartjs-2';
import { connect } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import 'react-table/react-table.css';
import { addToIndicators, fetchIndicators, reorderIndicators } from '../../actions';
import GraphCard from './GraphCard';
import NumberCard from './NumberCard';
import './tablero.scss';

// Disable charts legends by default.
defaults.global.legend = false;

const numberData = [
  {
    title: 'Bin Location Summary',
    number: 2696,
    subtitle: 'In stock',
    id: Math.random()
  }, {
    title: 'Bin Location Summary',
    number: 1082,
    subtitle: 'Out of stock',
    id: Math.random()
  }, {
    title: 'Stock Movements',
    number: 468,
    subtitle: 'Not shipped',
    id: Math.random()
  }, {
    title: 'User Incomplete Tasks',
    number: 188,
    subtitle: 'Not completed',
    id: Math.random()
  }, {
    title: 'Discrepancy',
    number: 290,
    subtitle: 'Items received',
    id: Math.random()
  },
];

const SortableCards = SortableContainer(({ data }) => (
  <div className="cardComponent">
    {data.map((value, index) => (
      <GraphCard
        key={`item-${value.id}`}
        index={index}
        cardTitle={value.title}
        cardType={value.type}
        data={value.data} />
    ))}
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
        cardSubtitle={value.subtitle} />
    ))}
  </div>
);

class Tablero extends Component {
  dataFetched = false;

  constructor(props) {
    super(props);
  }

  componentDidMount() {
    this.fetchData();
  }

  fetchData() {
    this.props.fetchIndicators();
  }

  render() {
    return (
      <div className="cardsContainer">
        <NumberCardsRow data={numberData} />
        <SortableCards data={this.props.indicatorsData} onSortEnd={this.props.reorderIndicators} axis="xy" useDragHandle />
      </div>
    );
  }
}

const mapStateToProps = state => ({
  indicatorsData: state.indicators.data,
});

export default connect(mapStateToProps, {
  fetchIndicators, addToIndicators, reorderIndicators,
})(Tablero);
