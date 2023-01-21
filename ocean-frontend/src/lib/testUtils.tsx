import React from 'react';
import { render } from '@testing-library/react';
import { QueryClientProvider } from 'react-query';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { queryClient } from './queryClient';
import { AuthenticationProvider } from '../hooks/useAuthentication';
import { AxiosRequestConfig, AxiosResponse } from 'axios';

export function createTestUtils() {
  function _render(component: JSX.Element) {
    const router = createBrowserRouter([
      {
        path: '/',
        element: <AuthenticationProvider />,
        children: [
          {
            path: '/',
            element: component,
          },
          {
            path: '/signin',
            element: component,
          },
        ],
      },
    ]);

    return render(
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
      </QueryClientProvider>
    );
  }

  return {
    render: _render,
  };
}

interface MockResponseOptions<T> {
  data: T;
  status?: number;
  statusText?: string;
  headers?: Record<string, string>;
}

export function mockAxiosResponse<T>({
  data,
  status = 200,
  statusText = 'OK',
  headers = {},
}: MockResponseOptions<T>): AxiosResponse<T> {
  return {
    data,
    status,
    statusText,
    headers,
    config: undefined as any as AxiosRequestConfig,
  };
}
