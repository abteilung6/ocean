import React from 'react';
import { screen } from '@testing-library/react';
import { createTestUtils, mockAxiosResponse, mockProject } from '../../lib/testUtils';
import api from '../../lib/api';
import { ProjectListPage } from '.';

const firstProject = mockProject({ projectId: 1, name: 'project-1' });
const secondProject = mockProject({ projectId: 2, name: 'project-2' });
const projects = [firstProject, secondProject];

describe(ProjectListPage.name, () => {
  const { render } = createTestUtils();

  test('should render empty state', async () => {
    jest.spyOn(api.Project, 'getApiProjects').mockResolvedValue(
      mockAxiosResponse({
        data: [],
      })
    );
    render(<ProjectListPage />);
    await screen.findByText('No projects');
  });

  test('should render projects', async () => {
    jest.spyOn(api.Project, 'getApiProjects').mockResolvedValue(
      mockAxiosResponse({
        data: projects,
      })
    );
    render(<ProjectListPage />);
    await screen.findByText('project-1');
  });
});
