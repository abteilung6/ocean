import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TextInput } from './TextInput';

describe(TextInput.name, () => {
  const user = userEvent.setup();
  test('should change the value', async () => {
    render(<TextInput label="My label" name="my-label" />);

    // Accessibility check by label
    const input = screen.getByLabelText('My label');
    expect(input).toHaveValue('');

    await user.type(input, 'changed');
    await waitFor(() => expect(input).toHaveValue('changed'));
  });
});
