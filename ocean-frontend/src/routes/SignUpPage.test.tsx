import React from 'react';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {
  createTestUtils,
  mockAccount,
  mockAxiosResponse,
  mockRegisterAccountRequest,
} from '../lib/testUtils';
import api from '../lib/api';
import { SignUpPage } from './SignUpPage';
import { RegisterAccountRequest } from '../openapi-generated';

describe(SignUpPage.name, () => {
  const user = userEvent.setup();
  const { render } = createTestUtils();

  beforeEach(() => {
    render(<SignUpPage />);
  });

  const updateFormWith = async (values: RegisterAccountRequest) => {
    await user.type(screen.getByLabelText('Email address'), values.email);
    await user.type(screen.getByLabelText('Password'), values.password);
    await user.type(screen.getByLabelText('First name'), values.firstname);
    await user.type(screen.getByLabelText('Last name'), values.lastname);
    await user.type(screen.getByLabelText('Company'), values.company);
  };

  test('should enable submit button when form is valid', async () => {
    expect(screen.getByRole('button', { name: 'Register' })).toBeDisabled();
    const registerAccountRequest = mockRegisterAccountRequest();
    await updateFormWith(registerAccountRequest);
    expect(screen.getByRole('button', { name: 'Register' })).toBeEnabled();
  });

  test('should register when submit button is clicked', async () => {
    const account = mockAccount();
    const spyPostApiAuthRegister = jest
      .spyOn(api.Authentication, 'postApiAuthRegister')
      .mockResolvedValue(
        mockAxiosResponse({
          data: account,
        })
      );

    const registerAccountRequest = mockRegisterAccountRequest();
    await updateFormWith(registerAccountRequest);
    const button = screen.getByRole('button', { name: 'Register' });
    expect(button).toBeEnabled();
    await user.click(button);

    expect(spyPostApiAuthRegister).toBeCalledWith(registerAccountRequest);
  });
});
