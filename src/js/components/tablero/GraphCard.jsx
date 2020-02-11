import PropTypes from 'prop-types';
import React from 'react';
import { Line } from 'react-chartjs-2';
import { SortableElement, sortableHandle } from 'react-sortable-hoc';

const DragHandle = sortableHandle(() => <span className="dragHandler">::</span>);

const GraphCard = SortableElement(({
    cardTitle, data,
}) => (
        <div className="graphCard">
            <div className="headerCard">
                <span className="titleCard"> {cardTitle} </span>
                <DragHandle />
            </div>
            <div className="contentCard">
                <Line data={data} width={632} height={300} />
            </div>
        </div>
    )
);

export default GraphCard;

GraphCard.propTypes = {
    cardTitle: PropTypes.string.isRequired,
    data: PropTypes.any.isRequired
};
