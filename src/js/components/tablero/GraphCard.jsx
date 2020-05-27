/* eslint-disable no-underscore-dangle */
import PropTypes from 'prop-types';
import React from 'react';
import { Bar, Doughnut, HorizontalBar, Line } from 'react-chartjs-2';
import { SortableElement } from 'react-sortable-hoc';
import DragHandle from './DragHandle';
import LoadingCard from './LoadingCard';
import Numbers from './Numbers';
import NumbersTableCard from './NumbersTableCard';
import TableCard from './TableCard';

const handleChartClick = (elements) => {
  const link = elements[0]._chart.data.datasets[0].links[elements[0]._index];

  if (link && link !== '') {
    window.location = link;
  }
};

const GraphCard = SortableElement(({
  cardId, cardTitle, cardType, cardLink, data, options, loadIndicator,
}) => {
  let graph;
  let filter = 0;
  let label = 'Last';
  if (cardType === 'line') {
    graph = (
      <Line
        data={data}
        options={options}
        onElementsClick={elements => handleChartClick(elements)}
      />
    );
    filter = 1;
    label = 'Next';
  } else if (cardType === 'bar') {
    graph = <Bar data={data} options={options} />;
    filter = 1;
  } else if (cardType === 'doughnut') {
    graph = <Doughnut data={data} options={options} />;
  } else if (cardType === 'horizontalBar') {
    graph = (<HorizontalBar
      data={data}
      options={options}
      onElementsClick={elements => handleChartClick(elements)}
    />);
  } else if (cardType === 'numbers') {
    graph = <Numbers data={data} options={options} />;
  } else if (cardType === 'table') {
    graph = <TableCard data={data} />;
    filter = 1;
  } else if (cardType === 'numberTable') {
    graph = <NumbersTableCard data={data} options={options} />;
  } else if (cardType === 'loading') {
    graph = <LoadingCard />;
  } else if (cardType === 'error') {
    graph = <button onClick={() => loadIndicator(cardId)} ><i className="fa fa-repeat" /></button>;
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
            onChange={e => loadIndicator(cardId, `querySize=${e.target.value}`)}
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
        <div className="graph-container">
          {graph}
        </div>
      </div>
    </div>
  );
});

export default GraphCard;

GraphCard.propTypes = {
  cardTitle: PropTypes.string,
  cardType: PropTypes.string.isRequired,
};
