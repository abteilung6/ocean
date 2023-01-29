import React from 'react';
import ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { QueryClientProvider } from 'react-query';
import reportWebVitals from './reportWebVitals';
import { queryClient } from './lib/queryClient';
import { AuthenticationProvider } from './hooks/useAuthentication';
import { SignInPage } from './routes/SignInPage';
import { DashboardPage } from './routes/DashboardPage';
import './index.css';
import { Routing } from './lib/routing';
import { SignUpPage } from './routes/SignUpPage';

export const browserRouter = createBrowserRouter([
  {
    path: '/',
    element: <AuthenticationProvider />,
    children: [
      {
        path: Routing.getSignInRoute(),
        element: <SignInPage />,
      },
      {
        path: Routing.getSignUpRoute(),
        element: <SignUpPage />,
      },
      {
        path: '/',
        element: <DashboardPage />,
      },
    ],
  },
]);

const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);
root.render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={browserRouter} />
    </QueryClientProvider>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
