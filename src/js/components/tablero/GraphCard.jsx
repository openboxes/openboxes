import PropTypes from 'prop-types';
import React from 'react';
import { Line, Bar, Doughnut, HorizontalBar } from 'react-chartjs-2';
import { SortableElement, sortableHandle } from 'react-sortable-hoc';
import LoadingCard from './LoadingCard';
import Numbers from './Numbers';
import { loadColors } from '../../consts/dataFormat/dataLoading';


const DragHandle = sortableHandle(() => (
  <span className="dragHandler">::</span>
));

const GraphCard = SortableElement(({
  cardTitle, cardType, cardLink, data,
}) => {
  const cardData = data;
  let graph;
  if (cardType === 'line') {
    cardData.datasets = loadColors(data, 'line');
    graph = <Line data={data} />;
  } else if (cardType === 'bar') {
    cardData.datasets = loadColors(data, 'bar');
    graph = <Bar data={data} />;
  } else if (cardType === 'doughnut') {
    cardData.datasets = loadColors(data, 'doughnut');
    graph = <Doughnut data={data} />;
  } else if (cardType === 'horizontalBar') {
    cardData.datasets = loadColors(data, 'horizontalBar');
    graph = <HorizontalBar data={data} />;
  } else if (cardType === 'numbers') {
    graph = <Numbers data={data} />;
  } else if (cardType === 'loading') {
    graph = <LoadingCard />;
  } else if (cardType === 'error') {
    graph = <i className="fa fa-repeat" />;
  }

  return (
    <div className={`graphCard ${cardType === 'error' ? 'errorCard' : ''}`}>
      <div className="headerCard">
        {cardLink ?
          <a target="_blank" rel="noopener noreferrer" href={cardLink} className="titleLink">
            <span className="titleLink"> {cardTitle} </span>
          </a>
          :
          <span className="titleLink"> {cardTitle} </span>
        }
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
};

