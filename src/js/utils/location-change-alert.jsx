import { confirmAlert } from 'react-confirm-alert';

import { dispatch } from '../store';
import { changeCurrentLocation } from '../actions';

const showLocationChangedAlert = (translate, oldLocation, newLocation, onLocationChanged) => {
  if (oldLocation.id && oldLocation.id !== newLocation.id) {
    confirmAlert({
      title: translate('message.locationChanged.label', 'Location was changed'),
      message: translate(
        'locationChanged.message',
        'Current location was changed, do you want to change the location back and continue work? If you press No all not saved changes will be lost.',
      ),
      buttons: [
        {
          label: translate('default.yes.label', 'Yes'),
          onClick: () => { dispatch(changeCurrentLocation(oldLocation)); },
        },
        {
          label: translate('default.no.label', 'No'),
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
