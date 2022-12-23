import axios from 'axios';
import {
  AccountApi,
  AuthenticationApi,
  Configuration,
  MemberApi,
  ProjectApi,
} from '../openapi-generated';

export const axiosInstance = axios.create();

const configuration = new Configuration({
  basePath: process.env.REACT_APP_API_DOMAIN,
});

/**
 * Usage:
 * - api.Authentication.postApiAuthRegister({});
 */
export default {
  Authentication: new AuthenticationApi(configuration, '', axiosInstance),
  Account: new AccountApi(configuration, '', axiosInstance),
  Member: new MemberApi(configuration, '', axiosInstance),
  Project: new ProjectApi(configuration, '', axiosInstance),
};
