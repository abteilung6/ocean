import React from 'react';
import { Button } from '../components/Button/Button';
import { SplitLayout } from '../components/SplitLayout/SplitLayout';
import { TextInput } from '../components/TextInput/TextInput';

export const SignInPage: React.FC = () => {
  return (
    <SplitLayout
      left={
        <div>
          <img
            className="h-12 w-auto"
            src={process.env.REACT_APP_LOGO_URL}
            alt={process.env.REACT_APP_BRAND_NAME}
          />
          <h2 className="mt-6 text-3xl font-bold tracking-tight text-gray-900">
            Sign in to your account
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            or{' '}
            <a href="#" className="font-medium text-indigo-600 hover:text-indigo-500">
              create your account
            </a>
          </p>
          <div className="mt-6">
            <TextInput id="email" name="email" type="email" label="Email address" />
          </div>
          <div className="mt-6">
            <TextInput id="password" name="password" type="password" label="Password" />
          </div>
          <div className="mt-6">
            <Button fullWidth>Sign in</Button>
          </div>
        </div>
      }
      right={
        <img
          className="absolute inset-0 h-full w-full object-cover"
          src={process.env.REACT_APP_SIGIN_BACKGROUND_IMAGE}
          alt="sign in background"
        />
      }
    />
  );
};
