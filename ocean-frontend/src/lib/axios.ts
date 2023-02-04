import axios, { AxiosError } from 'axios';
import { ResponseError } from '../openapi-generated';

const headers = {
  'Content-Type': 'application/json',
  'Access-Control-Allow-Origin': '*',
};

export const axiosPlatformInstance = axios.create({
  baseURL: process.env.REACT_APP_API_DOMAIN,
  headers,
});

axiosPlatformInstance.interceptors.request.use((axiosRequestConfig) => {
  const accessToken = localStorage.getItem('accessToken');
  if (accessToken) {
    if (axiosRequestConfig.headers?.set && typeof axiosRequestConfig.headers.set === 'function') {
      axiosRequestConfig.headers.set('Authorization', `Bearer ${accessToken}`, true);
    }
  } else {
    if (
      axiosRequestConfig.headers?.delete &&
      typeof axiosRequestConfig.headers.delete === 'function'
    ) {
      axiosRequestConfig.headers.delete('Authorization');
    }
  }
  return axiosRequestConfig;
});

axiosPlatformInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response && error.response.status >= 400 && error.response.status <= 500) {
      const axiosError = error as AxiosError<ResponseError>;
      return await Promise.reject(new Error(axiosError.response?.data.message));
    } else if (error.code === 'ERR_NETWORK') {
      return await Promise.reject(new Error('Service Unavailable'));
    } else {
      return await Promise.reject(error);
    }
  }
);
