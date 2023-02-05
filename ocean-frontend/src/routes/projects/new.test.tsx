import React from 'react';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {
  createTestUtils,
  mockAxiosResponse,
  mockCreateProjectRequest,
  mockProject,
} from '../../lib/testUtils';
import api from '../../lib/api';
import { ProjectCreatePage } from './new';

describe(ProjectCreatePage.name, () => {
  const user = userEvent.setup();
  const { render } = createTestUtils();

  beforeEach(() => {
    render(<ProjectCreatePage />);
  });

  test('should enable submit button when form is valid', async () => {
    expect(screen.getByRole('button', { name: 'Create project' })).toBeDisabled();
    await user.type(screen.getByLabelText('Name'), 'project-1');
    await user.type(screen.getByLabelText('Description'), 'A short description');
    expect(screen.getByRole('button', { name: 'Create project' })).toBeEnabled();
  });

  test('should create project when submit button is clicked', async () => {
    const project = mockProject();
    const spyPostApiAuthRegister = jest.spyOn(api.Project, 'postApiProjects').mockResolvedValue(
      mockAxiosResponse({
        data: mockProject(),
      })
    );
    await user.type(screen.getByLabelText('Name'), project.name);
    await user.type(screen.getByLabelText('Description'), project.description);
    await user.click(screen.getByRole('button', { name: 'Create project' }));

    expect(spyPostApiAuthRegister).toBeCalledWith(
      mockCreateProjectRequest({ name: project.name, description: project.description })
    );
  });
});
