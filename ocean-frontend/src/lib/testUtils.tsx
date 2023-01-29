import React from 'react';
import { render } from '@testing-library/react';
import { QueryClientProvider } from 'react-query';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { queryClient } from './queryClient';
import { AuthenticationProvider } from '../hooks/useAuthentication';
import { AxiosRequestConfig, AxiosResponse } from 'axios';
import { Account, AuthenticatorType, RegisterAccountRequest } from '../openapi-generated';

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

export function mockRegisterAccountRequest(
  overrides?: Partial<RegisterAccountRequest>
): RegisterAccountRequest {
  return {
    email: overrides?.email ?? 'bob@localhost.com',
    username: overrides?.username ?? '',
    password: overrides?.password ?? '1234565ABC',
    firstname: overrides?.firstname ?? 'John',
    lastname: overrides?.lastname ?? 'Doe',
    company: overrides?.company ?? 'Bob Ltd',
  };
}

export function mockAccount(): Account {
  return {
    accountId: 1,
    email: 'bob@localhost.com',
    firstname: 'John',
    lastname: 'Doe',
    company: 'Bob Ltd',
    createdAt: '',
    authenticatorType: AuthenticatorType.Credentials,
    verified: true,
    passwordHash: '',
  };
}
