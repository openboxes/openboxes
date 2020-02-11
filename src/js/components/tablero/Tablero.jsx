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
            cardTitle={'S.M not received'}
            cardNumber={468}
            cardSubtitle={'N° of S.M not shipped'} />
          <NumberCard
            cardTitle={'User incomplete tasks'}
            cardNumber={188}
            cardSubtitle={'N° of put aways not completed'} />
          <NumberCard
            cardTitle={'Discrepancy'}
            cardNumber={290}
            cardSubtitle={'N° of items received'} />
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
