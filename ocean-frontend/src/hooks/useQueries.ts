import { useMutation, UseMutationOptions } from 'react-query';
import api from '../lib/api';
import { AuthResponse, SignInRequest } from '../openapi-generated';

export const useSignInMutation = (
  options?: Omit<UseMutationOptions<AuthResponse, Error, SignInRequest>, 'mutationFn'>
) =>
  useMutation<AuthResponse, Error, SignInRequest>(async (signInRequest) => {
    const { data } = await api.Authentication.postApiAuthSignin('credentials', signInRequest);
    return data;
  }, options);
