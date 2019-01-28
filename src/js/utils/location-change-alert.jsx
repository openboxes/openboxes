import { confirmAlert } from 'react-confirm-alert';

import { dispatch } from '../store';
import { changeCurrentLocation } from '../actions';

const showLocationChangedAlert = (translate, oldLocation, newLocation, onLocationChanged) => {
  if (oldLocation.id && oldLocation.id !== newLocation.id) {
    confirmAlert({
      title: translate('message.locationChanged.label'),
      message: translate('locationChanged.message'),
      buttons: [
        {
          label: translate('default.yes.label'),
          onClick: () => { dispatch(changeCurrentLocation(oldLocation)); },
        },
        {
          label: translate('default.no.label'),
          onClick: () => {
            if (onLocationChanged) {
              onLocationChanged();
            }
          },
        },
      ],
    });
  }
};

export default showLocationChangedAlert;
