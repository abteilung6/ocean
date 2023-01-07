import axios from 'axios';

const headers = {
  'Content-Type': 'application/json',
  'Access-Control-Allow-Origin': '*',
};

export const axiosPlatformInstance = axios.create({
  baseURL: process.env.REACT_APP_API_DOMAIN,
  headers,
});
