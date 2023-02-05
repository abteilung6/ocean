import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useProjectsQuery } from '../../hooks/useQueries';
import { Routing } from '../../lib/routing';
import { EmptyState } from '../../components/EmptyState/EmptyState';

export const ProjectListPage: React.FC = () => {
  const navigate = useNavigate();
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
          onClick={() => navigate(Routing.getProjectCreateRoute())}
        />
      </div>
    );
  }

  return <div>ProjectsListPage</div>;
};
