/* eslint-disable no-param-reassign */
/* eslint-disable react/prop-types */
import PropTypes from 'prop-types';
import React from 'react';
import { Line, Bar, Doughnut, HorizontalBar } from 'react-chartjs-2';
import { SortableElement, sortableHandle } from 'react-sortable-hoc';
import LoadingCard from './LoadingCard';

import { loadColors } from '../../../assets/dataFormat/dataLoading';

const Numbers = ({ data }) => (
  <div className="gyrIndicator">
    <div className="numberIndicator">
      <div className="value">
        <div className="circle green" /> {data.green.value}
      </div>
      <div className="subtitle">{data.green.subtitle}</div>
    </div>
    <div className="numberIndicator">
      <div className="value">
        <div className="circle yellow" /> {data.yellow.value}
      </div>
      <div className="subtitle">{data.yellow.subtitle}</div>
    </div>
    <div className="numberIndicator">
      <div className="value">
        <div className="circle red" /> {data.red.value}
      </div>
      <div className="subtitle">{data.red.subtitle}</div>
    </div>
  </div>
);

const DragHandle = sortableHandle(() => (
  <span className="dragHandler">::</span>
));

let graphClass = 'graphCard';
const GraphCard = SortableElement(({ cardTitle, cardType, data }) => {
  let graph;
  if (cardType === 'line') {
    data.datasets = loadColors(data, 'line');
    graph = <Line data={data} />;
    graphClass = 'graphCard';
  } else if (cardType === 'bar') {
    data.datasets = loadColors(data, 'bar');
    graph = <Bar data={data} />;
    graphClass = 'graphCard';
  } else if (cardType === 'doughnut') {
    data.datasets = loadColors(data, 'doughnut');
    graph = <Doughnut data={data} />;
    graphClass = 'graphCard';
  } else if (cardType === 'horizontalBar') {
    data.datasets = loadColors(data, 'horizontalBar');
    graph = <HorizontalBar data={data} />;
    graphClass = 'graphCard';
  } else if (cardType === 'numbers') {
    graph = <Numbers data={data} />;
    graphClass = 'graphCard';
  } else if (cardType === 'loading') {
    graph = <LoadingCard />;
    graphClass = 'graphCard';
  } else if (cardType === 'error') {
    graph = <i className="fa fa-repeat" />;
    graphClass = 'graphCard errorCard';
  }

  return (
    <div className={graphClass}>
      <div className="headerCard">
        <span className="titleCard"> {cardTitle} </span>
        <DragHandle />
      </div>
      <div className="contentCard">{graph}</div>
    </div>
  );
});

export default GraphCard;

GraphCard.propTypes = {
  cardTitle: PropTypes.string.isRequired,
  cardType: PropTypes.string.isRequired,
  // eslint-disable-next-line react/forbid-prop-types
  data: PropTypes.any.isRequired,
};
