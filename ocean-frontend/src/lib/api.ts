import {
  AccountApi,
  AuthenticationApi,
  Configuration,
  MemberApi,
  ProjectApi,
} from '../openapi-generated';
import { axiosPlatformInstance } from './axios';

const configuration = new Configuration({
  basePath: process.env.REACT_APP_API_DOMAIN,
});

/**
 * Usage:
 * - api.Authentication.postApiAuthRegister({});
 */
export default {
  Authentication: new AuthenticationApi(configuration, '', axiosPlatformInstance),
  Account: new AccountApi(configuration, '', axiosPlatformInstance),
  Member: new MemberApi(configuration, '', axiosPlatformInstance),
  Project: new ProjectApi(configuration, '', axiosPlatformInstance),
};
