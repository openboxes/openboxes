import React, { Component } from "react";
import { defaults } from "react-chartjs-2";
import { connect } from "react-redux";
import { SortableContainer } from "react-sortable-hoc";
import "react-table/react-table.css";
import {
  addToIndicators,
  fetchIndicators,
  reorderIndicators
} from "../../actions";
import GraphCard from "./GraphCard";
import NumberCard from "./NumberCard";
import "./tablero.scss";
import apiClient from "../../utils/apiClient";

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
        data={value.data}
      />
    ))}
  </div>
));

const NumberCardsRow = ({ data }) => (
  <div className={data.length ? "cardComponent" : "loading-numbers"}>
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

class Tablero extends Component {
  dataFetched = false;
  state = {
    numberData: []
  };

  constructor(props) {
    super(props);
  }

  componentDidMount() {
    this.fetchData();
  }

  fetchNumbersData() {
    const url = "/openboxes/apitablero/getNumberData";
    apiClient.get(url).then(res => {
      this.setState({ numberData: res.data });
    });
  }

  fetchData() {
    this.props.fetchIndicators();
    this.fetchNumbersData();
  }

  render() {
    return (
      <div className="cardsContainer">
        <NumberCardsRow data={this.state.numberData} />
        <SortableCards
          data={this.props.indicatorsData}
          onSortEnd={this.props.reorderIndicators}
          axis="xy"
          useDragHandle
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
  reorderIndicators
})(Tablero);
