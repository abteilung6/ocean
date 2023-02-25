import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useProjectsQuery } from '../../hooks/useQueries';
import { Routing } from '../../lib/routing';
import { EmptyState } from '../../components/EmptyState/EmptyState';
import {
  DataCell,
  HeaderCell,
  Table,
  TableBody,
  TableHead,
  TableRow,
} from '../../components/Table/Table';
import { Button } from '../../components/Button/Button';
import { format } from 'date-fns';
import { Project } from '../../openapi-generated';

export const ProjectListPage: React.FC = () => {
  const navigate = useNavigate();
  const projectsQuery = useProjectsQuery();
  const projects = projectsQuery.data ?? [];

  if (projectsQuery.isFetching) {
    return <div>loading</div>;
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
  } else {
    return (
      <div>
        <div className="md:flex md:items-center md:justify-between mb-6">
          <div className="min-w-0 flex-1">
            <h2 className="text-2xl font-bold leading-7 text-gray-900 sm:truncate sm:text-3xl sm:tracking-tight">
              Projects
            </h2>
          </div>
          <div className="mt-4 flex md:mt-0 md:ml-4">
            <Button onClick={() => navigate(Routing.getProjectCreateRoute())}>
              Create project
            </Button>
          </div>
        </div>
        <Table>
          <TableHead>
            <TableRow>
              <HeaderCell>Name</HeaderCell>
              <HeaderCell textAlignment="text-right">Created at</HeaderCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {[...projects].sort(compareProjectByName).map((project, index) => {
              const date = Date.parse(project.createdAt);
              return (
                <TableRow key={index}>
                  <DataCell onClick={() => console.log('Not implemented yet')}>
                    {project.name}
                  </DataCell>
                  <DataCell textAlignment="text-right">{format(date, 'dd/MM/yyyy')}</DataCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </div>
    );
  }
};

const compareProjectByName = (left: Project, right: Project): number => {
  if (left.name < right.name) {
    return -1;
  }
  if (left.name > right.name) {
    return 1;
  } else {
    return 0;
  }
};
