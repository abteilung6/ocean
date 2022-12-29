import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import { TextInput } from './TextInput';

export default {
  title: 'Components/TextInput',
  component: TextInput,
  argTypes: { onChange: { action: 'changed' } },
} as ComponentMeta<typeof TextInput>;

const Template: ComponentStory<typeof TextInput> = (args) => <TextInput {...args} />;

export const Default = Template.bind({});
Default.args = {
  label: 'Username',
  placeholder: 'Placeholder',
};

export const Invalid = Template.bind({});
Invalid.args = {
  label: 'Username',
  value: 'wrong value',
  isValid: false,
  validationError: 'Your username must be less than 4 characters.',
};

export const Disabled = Template.bind({});
Disabled.args = {
  label: 'Username',
  disabled: true,
};
