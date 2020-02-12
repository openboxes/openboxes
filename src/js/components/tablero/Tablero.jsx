import React, { Component } from 'react';
import { defaults } from 'react-chartjs-2';
import { connect } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import 'react-table/react-table.css';
import { addToIndicators, fetchIndicators, reorderIndicators } from '../../actions';
import GraphCard from './GraphCard';
import NumberCard from './NumberCard';

// Disable charts legends by default.
defaults.global.legend = false;

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

class Tablero extends Component {
  dataFetched = false;

  constructor(props) {
    super(props);
  }

  componentDidMount() {
    this.fetchData();
  }

  // componentWillReceiveProps(nextProps) {
  //   console.log(nextProps);
  // }

  fetchData() {
    this.props.fetchIndicators();
  }

  render() {
    return (
      <div className="cardsContainer">
        <div className="cardComponent">
          <NumberCard
            cardTitle={'Bin Location Summary'}
            cardNumber={2696}
            cardSubtitle={'In stock'} />
          <NumberCard
            cardTitle={'Bin Location Summary'}
            cardNumber={1082}
            cardSubtitle={'Out of stock'} />
          <NumberCard
            cardTitle={'Stock Movements'}
            cardNumber={468}
            cardSubtitle={'Not shipped'} />
          <NumberCard
            cardTitle={'User Incomplete Tasks'}
            cardNumber={188}
            cardSubtitle={'Not completed'} />
          <NumberCard
            cardTitle={'Discrepancy'}
            cardNumber={290}
            cardSubtitle={'Items received'} />
        </div>
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
