import PropTypes from 'prop-types';
import React from 'react';
import { Line, Bar, Doughnut, HorizontalBar } from 'react-chartjs-2';
import { SortableElement, sortableHandle } from 'react-sortable-hoc';
import LoadingCard from './LoadingCard';
import Numbers from './Numbers';
import TableCard from './TableCard';
import NumbersTableCard from './NumbersTableCard';
import { loadColors, loadOptions } from '../../consts/dataFormat/dataLoading';

// getColors loads indicator colors if it doesn't have defined colors yet
function getColors(data, type) {
  if (data.datasets.length !== 0) {
    if (data.datasets[0].borderColor || data.datasets[0].backgroundColor) {
      return data.datasets;
    }
  }
  return loadColors(data, type);
}

const DragHandle = sortableHandle(() => (
  <span className="dragHandler">::</span>
));

const GraphCard = SortableElement(({
  cardMethod, cardId, cardTitle, cardType, cardLink, data, reloadIndicator,
}) => {
  const cardData = data;
  let graph;
  let filter = 0;
  let label = 'Last';
  if (cardType === 'line') {
    cardData.datasets = getColors(data, 'line');
    graph = <Line data={data} options={loadOptions()} />;
    filter = 1;
    label = 'Next';
  } else if (cardType === 'bar') {
    cardData.datasets = getColors(data, 'bar');
    graph = <Bar data={data} options={loadOptions(cardMethod !== 'getFillRate')} />;
    filter = 1;
  } else if (cardType === 'doughnut') {
    cardData.datasets = getColors(data, 'doughnut');
    graph = <Doughnut data={data} options={loadOptions()} />;
  } else if (cardType === 'horizontalBar') {
    cardData.datasets = getColors(data, 'horizontalBar');
    graph = <HorizontalBar data={data} options={loadOptions()} />;
  } else if (cardType === 'numbers') {
    console.log(data);
    graph = <Numbers data={data} />;
  } else if (cardType === 'table') {
    console.log(data);
    graph = <TableCard data={data} />;
    filter = 1;
  } else if (cardType === 'numberTable') {
    console.log(data);
    graph = <NumbersTableCard data={data} />;
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
      <div className="contentCard">
        <div className="dataFilter">
          <select
            className={filter ? 'customSelect' : 'customSelect disabled'}
            onChange={e => reloadIndicator(cardMethod, cardType, cardTitle, cardLink, cardId, `querySize=${e.target.value}`)}
            disabled={!filter}
            defaultValue="6"
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
  cardTitle: PropTypes.string.isRequired,
  cardType: PropTypes.string.isRequired,
};
