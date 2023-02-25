import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';
import { DataCell, HeaderCell, Table, TableBody, TableHead, TableRow } from './Table';

export default {
  title: 'Display/Table',
  component: Table,
} as ComponentMeta<typeof Table>;

const rows = [
  { name: 'project-1', createdAt: '2 seconds ago' },
  { name: 'project-2', createdAt: '5 minutes ago' },
];

const Template: ComponentStory<typeof Table> = (args) => (
  <Table {...args}>
    <TableHead>
      <TableRow>
        <HeaderCell>Name</HeaderCell>
        <HeaderCell textAlignment="text-right">Created at</HeaderCell>
      </TableRow>
    </TableHead>
    <TableBody>
      {rows.map(({ name, createdAt }) => (
        <TableRow key={name}>
          <DataCell>{name}</DataCell>
          <DataCell textAlignment="text-right">{createdAt}</DataCell>
        </TableRow>
      ))}
    </TableBody>
  </Table>
);

export const Default = Template.bind({});
Default.args = {};
