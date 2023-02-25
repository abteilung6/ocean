import React from 'react';
import { render } from '@testing-library/react';
import { QueryClientProvider } from 'react-query';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { queryClient } from './queryClient';
import { AuthenticationProvider } from '../hooks/useAuthentication';
import { AxiosRequestConfig, AxiosResponse } from 'axios';
import {
  Account,
  AuthenticatorType,
  CreateProjectRequest,
  Project,
  RegisterAccountRequest,
} from '../openapi-generated';

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
          {
            path: '/projects',
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
    createdAt: '2023-02-05T12:22:21.710296Z',
    authenticatorType: AuthenticatorType.Credentials,
    verified: true,
    passwordHash: '',
  };
}

export function mockProject(overrides?: Partial<Project>): Project {
  return {
    projectId: overrides?.projectId ?? 1,
    name: overrides?.name ?? 'project-1',
    description: overrides?.description ?? 'A short description',
    createdAt: overrides?.createdAt ?? '2023-02-05T12:22:21.710296Z',
    ownerId: overrides?.ownerId ?? 1,
  };
}

export function mockCreateProjectRequest(
  overrides?: Partial<CreateProjectRequest>
): CreateProjectRequest {
  return {
    name: overrides?.name ?? 'project-1',
    description: overrides?.description ?? 'A short description',
  };
}
