import { useMutation, UseMutationOptions, useQuery, UseQueryOptions } from 'react-query';
import api from '../lib/api';
import {
  Account,
  AuthResponse,
  CreateProjectRequest,
  Project,
  RefreshTokenRequest,
  RegisterAccountRequest,
  SignInRequest,
} from '../openapi-generated';

export const useSignInMutation = (
  options?: Omit<UseMutationOptions<AuthResponse, Error, SignInRequest>, 'mutationFn'>
) =>
  useMutation<AuthResponse, Error, SignInRequest>(async (signInRequest) => {
    const { data } = await api.Authentication.postApiAuthSignin('credentials', signInRequest);
    return data;
  }, options);

export const useRefreshTokenMutation = (
  options?: Omit<UseMutationOptions<AuthResponse, Error, RefreshTokenRequest>, 'mutationFn'>
) =>
  useMutation<AuthResponse, Error, RefreshTokenRequest>(async (refreshTokenRequest) => {
    const { data } = await api.Authentication.postApiAuthRefresh(refreshTokenRequest);
    return data;
  }, options);

export const useSignUpMutation = (
  options?: Omit<UseMutationOptions<Account, Error, RegisterAccountRequest>, 'mutationFn'>
) =>
  useMutation<Account, Error, RegisterAccountRequest>(async (registerAccountRequest) => {
    const { data } = await api.Authentication.postApiAuthRegister(registerAccountRequest);
    return data;
  }, options);

// Project

export const useProjectsQuery = (options?: UseQueryOptions<readonly Project[]>) => {
  return useQuery<readonly Project[]>(
    ['projects'],
    async () => {
      const { data } = await api.Project.getApiProjects();
      return data;
    },
    {
      ...options,
    }
  );
};

export const useProjectCreateMutation = (
  options?: Omit<UseMutationOptions<Project, Error, CreateProjectRequest>, 'mutationFn'>
) =>
  useMutation<Project, Error, CreateProjectRequest>(async (createProjectRequest) => {
    const { data } = await api.Project.postApiProjects(createProjectRequest);
    return data;
  }, options);
