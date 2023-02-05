import React from 'react';
import ReactDOM from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { QueryClientProvider } from 'react-query';
import reportWebVitals from './reportWebVitals';
import { AuthenticationProvider } from './hooks/useAuthentication';
import { queryClient } from './lib/queryClient';
import { Routing } from './lib/routing';
import { SignInPage } from './routes/SignInPage';
import { DashboardPage } from './routes/DashboardPage';
import { SignUpPage } from './routes/SignUpPage';
import { ProjectListPage } from './routes/projects';
import './index.css';
import { ProjectCreatePage } from './routes/projects/new';

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
      {
        path: Routing.getProjectsRoute(),
        element: <ProjectListPage />,
      },
      {
        path: Routing.getProjectCreateRoute(),
        element: <ProjectCreatePage />,
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
