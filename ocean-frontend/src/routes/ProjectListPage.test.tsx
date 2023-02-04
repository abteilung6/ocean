import React from 'react';
import { screen, waitFor } from '@testing-library/react';
import { createTestUtils, mockAxiosResponse } from '../lib/testUtils';
import api from '../lib/api';
import { ProjectListPage } from './ProjectListPage';

describe(ProjectListPage.name, () => {
  const { render } = createTestUtils();

  test('should render empty state', async () => {
    jest.spyOn(api.Project, 'getApiProjects').mockResolvedValue(
      mockAxiosResponse({
        data: [],
      })
    );
    render(<ProjectListPage />);
    await waitFor(() => screen.getByText('No projects'));
  });
});
