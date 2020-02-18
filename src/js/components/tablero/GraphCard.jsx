import PropTypes from 'prop-types';
import React from 'react';
import { Line, Bar, Doughnut, HorizontalBar } from 'react-chartjs-2';
import { SortableElement, sortableHandle } from 'react-sortable-hoc';

import { loadColors } from '../../../assets/dataFormat/dataLoading'

const Numbers = ({ data }) => (
    <div className="gyrIndicator">
        <div className="numberIndicator">
            <div className="value">
                <div className="circle green"></div> {data.green.value}
            </div>
            <div className="subtitle">{data.green.subtitle}</div>
        </div>
        <div className="numberIndicator">
            <div className="value">
                <div className="circle yellow"></div> {data.yellow.value}
            </div>
            <div className="subtitle">{data.yellow.subtitle}</div>
        </div>
        <div className="numberIndicator">
            <div className="value">
                <div className="circle red"></div> {data.red.value}
            </div>
            <div className="subtitle">{data.red.subtitle}</div>
        </div>
    </div>
);

const DragHandle = sortableHandle(() => <span className="dragHandler">::</span>);

const GraphCard = SortableElement(({
    cardTitle, cardType, data,
}) => {
    let graph;
    if (cardType === 'line') {
        data['datasets'][0] = loadColors(data, "line");
        graph = <Line data={data} />;
    }
     
    else if (cardType === 'bar') {
        data['datasets'][0] = loadColors(data, "bar");
        graph = <Bar data={data} />
    } else if (cardType === 'doughnut') {
        data['datasets'][0] = loadColors(data, "doughnut");
        graph = <Doughnut data={data} />
    } else if (cardType === 'horizontalBar') {
        data['datasets'][0] = loadColors(data, "horizontalBar");
        graph = <HorizontalBar data={data} />
    } else if (cardType === 'numbers') {
        graph = <Numbers data={data} />
    }


    return (
        <div className="graphCard">
            <div className="headerCard">
                <span className="titleCard"> {cardTitle} </span>
                <DragHandle />
            </div>
            <div className="contentCard">
                {graph}
            </div>
        </div>
    )
}
);

export default GraphCard;

GraphCard.propTypes = {
    cardTitle: PropTypes.string.isRequired,
    cardType: PropTypes.string.isRequired,
    data: PropTypes.any.isRequired
};
