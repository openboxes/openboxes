/* eslint-disable no-underscore-dangle */
import PropTypes from 'prop-types';
import React from 'react';
import { Bar, Doughnut, HorizontalBar, Line } from 'react-chartjs-2';
import { SortableElement } from 'react-sortable-hoc';
import { loadColors, loadOptions } from '../../consts/dataFormat/dataLoading';
import DragHandle from './DragHandle';
import LoadingCard from './LoadingCard';
import Numbers from './Numbers';
import NumbersTableCard from './NumbersTableCard';
import TableCard from './TableCard';

// getColors loads indicator colors if it doesn't have defined colors yet
function getColors(data, type) {
  if (data.datasets.length !== 0) {
    if (data.datasets[0].borderColor || data.datasets[0].backgroundColor) {
      return data.datasets;
    }
  }
  return loadColors(data, type);
}

const handleChartClick = (elements) => {
  const link = elements[0]._chart.data.datasets[0].links[elements[0]._index];

  if (link && link !== '') {
    window.location = link;
  }
};

const GraphCard = SortableElement(({
  cardId, cardTitle, cardType, cardLink, data, config, reloadIndicator,
}) => {
  const cardData = data;
  let graph;
  let filter = 0;
  let label = 'Last';
  if (cardType === 'line') {
    cardData.datasets = getColors(data, 'line');
    graph = (
      <Line
        data={data}
        options={loadOptions()}
        onElementsClick={elements => handleChartClick(elements)}
      />
    );
    filter = 1;
    label = 'Next';
  } else if (cardType === 'bar') {
    cardData.datasets = getColors(data, 'bar');
    graph = <Bar data={data} options={loadOptions(config.stacked)} />;
    filter = 1;
  } else if (cardType === 'doughnut') {
    cardData.datasets = getColors(data, 'doughnut');
    graph = <Doughnut data={data} options={loadOptions()} />;
  } else if (cardType === 'horizontalBar') {
    cardData.datasets = getColors(data, 'horizontalBar');
    graph = <HorizontalBar data={data} options={loadOptions()} />;
  } else if (cardType === 'numbers') {
    graph = <Numbers data={data} />;
  } else if (cardType === 'table') {
    graph = <TableCard data={data} />;
    filter = 1;
  } else if (cardType === 'numberTable') {
    graph = <NumbersTableCard data={data} />;
  } else if (cardType === 'loading') {
    graph = <LoadingCard />;
  } else if (cardType === 'error') {
    graph = <i className="fa fa-repeat" />;
  }

  return (
    <div className={`graph-card ${cardType === 'error' ? 'error-card' : ''}`}>
      <div className="header-card">
        {cardLink ?
          <a target="_blank" rel="noopener noreferrer" href={cardLink} className="title-link">
            <span className="title-link"> {cardTitle} </span>
          </a>
          :
          <span className="title-link"> {cardTitle} </span>
        }
        <DragHandle />
      </div>
      <div className="content-card">
        <div className={filter ? 'data-filter' : 'data-filter disabled'}>
          <select
            className="custom-select"
            onChange={e => reloadIndicator(cardType, cardTitle, cardLink, cardId, `querySize=${e.target.value}`)}
            disabled={!filter}
            defaultValue={data.labels ? data.labels.length : '6'}
          >
            <option value="1">{label} Month</option>
            <option value="3">{label} 3 Months</option>
            <option value="6">{label} 6 Months</option>
            <option value="12">{label} Year</option>
            <option value="24">{label} 2 Years</option>
          </select>
        </div>
        {graph}
      </div>
    </div>
  );
});

export default GraphCard;

GraphCard.propTypes = {
  cardTitle: PropTypes.string,
  cardType: PropTypes.string.isRequired,
};
