import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import { SplitLayout } from './SplitLayout';

export default {
  title: 'Layout/SplitLayout',
  component: SplitLayout,
} as ComponentMeta<typeof SplitLayout>;

const Template: ComponentStory<typeof SplitLayout> = (args) => (
  <div className="h-full bg-white">
    <SplitLayout {...args} />
  </div>
);

export const Default = Template.bind({});
Default.args = {
  left: <div className="bg-green-300" />,
  right: <div className="bg-red-300" />,
};
