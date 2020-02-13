import PropTypes from 'prop-types';
import React from 'react';
import { Line, Bar, Doughnut, HorizontalBar } from 'react-chartjs-2';
import { SortableElement, sortableHandle } from 'react-sortable-hoc';
import LoadingCard from './LoadingCard';
import './tablero.scss';

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

const ErrorCard = () => (
    <div>
        <i className="fa fa-repeat" />
    </div>
);

let graphClass = "graphCard";
const DragHandle = sortableHandle(() => <span className="dragHandler">::</span>);

const GraphCard = SortableElement(({
    cardTitle, cardType, data,
}) => {
    let graph;
    if (cardType === 'line') {
        graph = <Line data={data} />;
        graphClass = "graphCard";
    } else if (cardType === 'bar') {
        graph = <Bar data={data} />;
        graphClass = "graphCard";
    } else if (cardType === 'doughnut') {
        graph = <Doughnut data={data} />;
        graphClass = "graphCard";
    } else if (cardType === 'horizontalBar') {
        graph = <HorizontalBar data={data} />
    } else if (cardType === 'numbers') {
        graph = <Numbers data={data} />;
        graphClass = "graphCard";
    } else if (cardType === 'loading') {
        graph = <LoadingCard />;
        graphClass = "graphCard";
    } else if (cardType === 'error') {
        graph = <ErrorCard />;
        graphClass = "graphCard errorCard";
    }


    return (
        <div className={graphClass}>
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
