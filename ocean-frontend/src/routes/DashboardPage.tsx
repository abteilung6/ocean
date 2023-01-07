import React from 'react';
import { Button } from '../components/Button/Button';
import { useAuthentication } from '../hooks/useAuthentication';

export const DashboardPage: React.FC = () => {
  const { logout } = useAuthentication();
  return (
    <div>
      DashboardPage
      <div>
        <Button onClick={() => logout()}>Logout</Button>
      </div>
    </div>
  );
};
