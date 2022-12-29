import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from './components/Button/Button';
import { Routes } from './lib/routes';

export const App: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div>
      <h1 className="text-3xl font-bold underline">Hello World</h1>
      <Button onClick={() => navigate(Routes.getSignInPageRoute())}>Log in</Button>
    </div>
  );
};

export default App;
