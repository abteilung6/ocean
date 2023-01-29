import React from 'react';
import { screen } from '@testing-library/react';
import { SignInPage } from './SignInPage';
import userEvent from '@testing-library/user-event';

import { createTestUtils, mockAxiosResponse } from '../lib/testUtils';
import api from '../lib/api';

describe(SignInPage.name, () => {
  const user = userEvent.setup();
  const { render } = createTestUtils();

  beforeEach(() => {
    render(<SignInPage />);
  });

  test('should enable submit button when form is valid', async () => {
    const emailInput = screen.getByLabelText('Email address');
    const passwordInput = screen.getByLabelText('Password');

    expect(screen.getByRole('button', { name: 'Sign in' })).toBeDisabled();
    await user.type(emailInput, 'bob@localhost.com');
    await user.type(passwordInput, '123456ABC');

    expect(screen.getByRole('button', { name: 'Sign in' })).toBeEnabled();
  });

  test('should login when button is clicked', async () => {
    const spyPostApiAuthSignin = jest
      .spyOn(api.Authentication, 'postApiAuthSignin')
      .mockResolvedValue(
        mockAxiosResponse({
          data: { accessToken: 'accessToken', refreshToken: 'refreshToken' },
        })
      );

    await user.type(screen.getByLabelText('Email address'), 'bob@localhost.com');
    await user.type(screen.getByLabelText('Password'), '123456ABC');
    const button = screen.getByRole('button', { name: 'Sign in' });
    expect(button).toBeEnabled();
    await user.click(button);

    expect(spyPostApiAuthSignin).toBeCalledWith('credentials', {
      email: 'bob@localhost.com',
      password: '123456ABC',
    });
  });
});
