import React from 'react';
import { EmptyState } from '../components/EmptyState/EmptyState';
import { useProjectsQuery } from '../hooks/useQueries';

export const ProjectListPage: React.FC = () => {
  const projectsQuery = useProjectsQuery();
  const projects = projectsQuery.data ?? [];

  if (projectsQuery.isLoading) {
    return <div>loading...</div>;
  } else if (projects.length === 0) {
    return (
      <div className="mt-12">
        <EmptyState
          title="No projects"
          description="Get started by creating a new project."
          buttonTitle="New Project"
        />
      </div>
    );
  }

  return <div>ProjectsListPage</div>;
};
