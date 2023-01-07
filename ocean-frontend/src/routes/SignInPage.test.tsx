import React from 'react';
import { screen, waitFor } from '@testing-library/react';
import { SignInPage } from './SignInPage';
import userEvent from '@testing-library/user-event';

import { createTestUtils } from '../lib/testUtils';

describe(SignInPage.name, () => {
  const user = userEvent.setup();
  const { render } = createTestUtils();

  beforeEach(() => {
    render(<SignInPage />);
  });

  test('should disabled submit button until form is valid', async () => {
    const emailInput = screen.getByLabelText('Email address');
    const passwordInput = screen.getByLabelText('Password');

    expect(screen.getByRole('button', { name: 'Sign in' })).toBeDisabled();
    await user.type(emailInput, 'bob@localhost.com');
    await user.type(passwordInput, '123456ABC');

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Sign in' })).toBeEnabled();
    });
  });
});
