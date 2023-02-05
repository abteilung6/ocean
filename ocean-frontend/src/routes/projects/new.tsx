import React from 'react';
import { useNavigate } from 'react-router-dom';
import * as Yup from 'yup';
import { useFormik } from 'formik';
import { useProjectCreateMutation } from '../../hooks/useQueries';
import { Routing } from '../../lib/routing';
import { TextInput } from '../../components/TextInput/TextInput';
import { Button } from '../../components/Button/Button';
import { TextArea } from '../../components/TextArea/TextArea';
import { Alert } from '../../components/Alert/Alert';

export const projectCreateSchema = Yup.object({
  name: Yup.string().required('Name is required.'),
  description: Yup.string().required('Description is required.'),
});

export const ProjectCreatePage: React.FC = () => {
  const navigate = useNavigate();
  const projectCreateMutation = useProjectCreateMutation();
  const formik = useFormik({
    initialValues: {
      name: '',
      description: '',
    },
    validationSchema: projectCreateSchema,
    onSubmit: async (values) => {
      formik.setSubmitting(false);
      projectCreateMutation.mutateAsync(values).then(() => navigate(Routing.getProjectsRoute()));
    },
  });

  return (
    <div className="overflow-hidden bg-white py-16 px-6 lg:px-8 lg:py-24">
      <div className="relative mx-auto max-w-xl">
        <div className="text-center">
          <h2 className="text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl">
            Create a project
          </h2>
          <p className="mt-4 text-lg leading-6 text-gray-500">
            Create a project in order to manage your services. Invite other accounts to your project
            and share your managed infrastructure.
          </p>
        </div>
        <form className="mt-12" onSubmit={formik.handleSubmit}>
          <div className="mt-6">
            {projectCreateMutation.error?.message && (
              <Alert variant="error" description={projectCreateMutation.error?.message} />
            )}
          </div>
          <div className="mt-6">
            <TextInput
              id="name"
              name="name"
              type="text"
              label="Name"
              isValid={formik.touched.name && !formik.errors.name}
              validationError={formik.errors.name}
              value={formik.values.name}
              disabled={projectCreateMutation.isLoading}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
            />
          </div>
          <div className="mt-6">
            <TextArea
              id="description"
              name="description"
              label="Description"
              isValid={formik.touched.description && !formik.errors.description}
              validationError={formik.errors.description}
              value={formik.values.description}
              disabled={projectCreateMutation.isLoading}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
            />
          </div>
          <div className="mt-6">
            <Button
              type="submit"
              fullWidth
              disabled={!formik.isValid || formik.isSubmitting || !formik.dirty}
            >
              Create project
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
