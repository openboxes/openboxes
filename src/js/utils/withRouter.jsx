import React from 'react';

import { useLocation, useNavigate, useParams } from 'react-router-dom';

function withRouter(Component) {
  function ComponentWithRouterProp(props) {
    const location = useLocation();
    const navigate = useNavigate();
    const params = useParams();

    const match = { params };

    return (
      <Component
        {...props}
        location={location}
        navigate={navigate}
        match={match}
      />
    );
  }

  return ComponentWithRouterProp;
}

export default withRouter;
