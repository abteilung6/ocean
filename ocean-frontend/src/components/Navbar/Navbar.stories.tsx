import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import { Navbar } from './Navbar';

export default {
  title: 'Layout/Navbar',
  component: Navbar,
} as ComponentMeta<typeof Navbar>;

const Template: ComponentStory<typeof Navbar> = (args) => <Navbar {...args} />;

export const Default = Template.bind({});
Default.args = {
  paths: [
    { name: 'Projects', route: 'projects' },
    { name: 'Admin', route: 'admin' },
  ],
  selectedRoute: 'projects',
};
