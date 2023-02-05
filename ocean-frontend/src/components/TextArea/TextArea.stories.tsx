import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import { TextArea } from './TextArea';

export default {
  title: 'Components/TextArea',
  component: TextArea,
  argTypes: { onChange: { action: 'changed' } },
} as ComponentMeta<typeof TextArea>;

const Template: ComponentStory<typeof TextArea> = (args) => (
  <TextArea label="Description" {...args} />
);

export const Default = Template.bind({});
Default.args = {
  placeholder: 'Placeholder',
};

export const Invalid = Template.bind({});
Invalid.args = {
  value: 'wrong value',
  isValid: false,
  validationError: 'Your username must be less than 4 characters.',
};

export const Disabled = Template.bind({});
Disabled.args = {
  disabled: true,
};
