import React from 'react';

import PropTypes from 'prop-types';

import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import Translate from 'utils/Translate';

const ReadyToStageCard = ({ data, linkTarget }) => (
  <div className="table-card">
    <table>
      <thead>
        <tr>
          <th>
            <Translate id="react.dashboard.readyToStage.order.label" defaultMessage="Order" />
          </th>
          <th className="mid">
            <Translate id="react.dashboard.readyToStage.product.label" defaultMessage="Product" />
          </th>
          <th>
            <Translate id="react.dashboard.readyToStage.quantityPicked.label" defaultMessage="Qty Picked" />
          </th>
          <th>
            <Translate id="react.dashboard.readyToStage.location.label" defaultMessage="Location" />
          </th>
          <th>
            <Translate id="react.dashboard.readyToStage.picker.label" defaultMessage="Picker" />
          </th>
        </tr>
      </thead>
      <tbody>
        {data.length === 0 && (
          <tr>
            <td colSpan={5} className="text-center">
              <Translate
                id="react.dashboard.readyToStage.empty.label"
                defaultMessage="There is no outstanding staging work"
              />
            </td>
          </tr>
        )}
        {data.map((task) => (
          <tr key={task.id}>
            <td>
              <a href={STOCK_MOVEMENT_URL.show(task.requisitionId)} rel="noreferrer" target={linkTarget}>
                {task.requisitionNumber}
              </a>
            </td>
            <td className="mid">{task.product?.name}</td>
            <td>{task.quantityPicked}</td>
            <td>{task.outboundContainer?.name ?? task.location?.name}</td>
            <td>{task.pickedBy?.name}</td>
          </tr>
        ))}
      </tbody>
    </table>
  </div>
);

ReadyToStageCard.propTypes = {
  data: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    requisitionId: PropTypes.string,
    requisitionNumber: PropTypes.string,
    product: PropTypes.shape({ name: PropTypes.string }),
    quantityPicked: PropTypes.number,
    location: PropTypes.shape({ name: PropTypes.string }),
    outboundContainer: PropTypes.shape({ name: PropTypes.string }),
    pickedBy: PropTypes.shape({ name: PropTypes.string }),
  })).isRequired,
  linkTarget: PropTypes.string,
};

ReadyToStageCard.defaultProps = {
  linkTarget: undefined,
};

export default ReadyToStageCard;
