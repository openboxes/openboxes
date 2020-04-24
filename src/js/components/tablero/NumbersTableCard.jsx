/* eslint-disable no-undef */
import React from 'react';
import PropTypes from 'prop-types';

const NumbersTableCard = props => (
  <div className="delayedShipment">
    <div className="delayedShipmentNumber">
      <div className="shipmentNumberCard">
        <div> <img src={props.data.urlIconAir} alt="air" />{props.data.numberByAir} </div>
        <div>{props.data.labelByAir}</div>
      </div>
      <div className="shipmentNumberCard">
        <div> <img src={props.data.urlIconSea} alt="sea" /> {props.data.numberBySea} </div>
        <div>{props.data.labelBySea}</div>
      </div>
      <div className="shipmentNumberCard">
        <div> <img src={props.data.urlIconLand} alt="land" /> {props.data.numberByLand} </div>
        <div>{props.data.labelByLand}</div>
      </div>
    </div>
    <div className="tableCard">
      <table>
        <thead>
          <tr>
            <th>{props.data.labelShipment}</th>
            <th className="end">{props.data.labelName}</th>
          </tr>
        </thead>
        <tbody>
          {props.data.shipmentsData.map(item => (
            <tr
              onClick={() => window.open(item.link, '_blank')}
              key={`item-${item.number}`}
              className="tableLink"
            >
              <td>{item.number}</td>
              <td className="mid">{_.truncate(item.name, { length: 80 })}</td>
            </tr>
        ))}
        </tbody>
      </table>
    </div>
  </div>
);

NumbersTableCard.propTypes = {
  data: PropTypes.shape({
    labelByAir: PropTypes.string,
    labelBySea: PropTypes.string,
    labelByLand: PropTypes.string,
    numberByAir: PropTypes.number,
    numberBySea: PropTypes.number,
    numberByLand: PropTypes.number,
    labelShipment: PropTypes.string,
    labelName: PropTypes.string,
    urlIconAir: PropTypes.string,
    urlIconSea: PropTypes.string,
    urlIconLand: PropTypes.string,
    shipmentsData: PropTypes.arrayOf(PropTypes.shape({})),
  }).isRequired,
};

export default NumbersTableCard;
