import React from 'react';
import { render, screen } from '@testing-library/react';
import { renderWithTestingRouter } from './lib/routes';
import App from './App';

test('renders app', () => {
  render(renderWithTestingRouter(<App />));
  expect(screen.getByText(/Hello World/i)).toBeInTheDocument();
});
