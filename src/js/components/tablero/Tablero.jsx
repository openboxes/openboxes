import React, { Component } from 'react';
import { defaults } from 'react-chartjs-2';
import { connect } from 'react-redux';
import { SortableContainer } from 'react-sortable-hoc';
import 'react-table/react-table.css';
import { addToIndicators, fetchIndicators, reorderIndicators } from '../../actions';
import GraphCard from './GraphCard';
import NumberCard from './NumberCard';
import './tablero.scss';
import { numberData } from '../../../assets/dataFormat/numberData'

// Disable charts legends by default.
defaults.global.legend = false;

//Should be on JSON package with charts Data

let hideArchive = true;

const SortableCards = SortableContainer(({ data, onDragStartHandle }) => (
  <div className="cardComponent">
    {data.map((value, index) => (
      <GraphCard
        key={`item-${value.id}`}
        index={index}
        cardTitle={value.title}
        cardType={value.type}
        data={value.data}
        onDragStartHandle={onDragStartHandle}
        onMouseDown={e => onDragStartHandle(e, cardTitle)}
      />
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
        cardSubtitle={value.subtitle}
      />
    ))}
  </div>
);

const ArchiveIndicator = ({ hideArchive, onDragOver, onDrop }) => (
  <div
    className={hideArchive ? "archiveDiv hideArchive" : "archiveDiv"}
    onDragOver={e => onDragOver(e)}
    onDrop={() => onDrop(e, "archived")}
  >
    <span>
      Archive indicator <i className="fa fa-archive"></i>
    </span>
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

  handle = () => {
    hideArchive = false;
    console.log(hideArchive);
  };

  onDragStart = (ev, id) => {
    hideArchive = !hideArchive;
    console.log("dragstart: ", id);
    //ev.dataTransfer.setData("text/plain", id);
  };

  onDragOver = ev => {
    console.log("dragOver: ", ev);
    ev.preventDefault();
  };

  onDrop = (ev, cat) => {
    let id = ev.dataTransfer.getData("text");

    console.log("Dropped:", id, cat);
    hideArchive = !hideArchive;
  };

  sortStartHandle = () => {
    console.log("start");
    //hideArchive = false;
  };
  sortMoveHandle = () => {
    console.log("move");
    //hideArchive = false;
  };
  sortEndHandle = () => {
    console.log("end");
    //hideArchive = true;
  };
  sortOverHandle = () => {
    console.log("sortOver");
  };

  render() {
    return (
      <div className="cardsContainer">
        <NumberCardsRow data={numberData} />
        <SortableCards
          data={this.props.indicatorsData}
          onSortStart={this.sortStartHandle()}
          onSortMove={this.sortMoveHandle()}
          onSortEnd={(this.sortEndHandle(), this.props.reorderIndicators)}
          handle={this.handle}
          axis="xy"
          useDragHandle
          onDragStartHandle={this.onDragStart}
          distance={1}
        />
        <ArchiveIndicator
          hideArchive={hideArchive}
          onSortOver={this.sortOverHandle()}
          onDrop={this.onDrop}
        />
        <div className="unarchive">
          <span>Unarchive indicator (2) </span>
          <i className="fa fa-archive"></i>
        </div>
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
