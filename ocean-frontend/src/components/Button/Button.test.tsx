import React from 'react';
import { render, screen } from '@testing-library/react';
import { Button } from './Button';

describe(Button.name, () => {
  test('should be able to disable', () => {
    render(<Button disabled>My button</Button>);
    expect(screen.getByRole('button', { name: 'My button' })).toBeDisabled();
  });
});
