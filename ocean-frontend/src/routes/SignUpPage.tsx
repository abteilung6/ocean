import React from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { Button } from '../components/Button/Button';
import { SplitLayout } from '../components/SplitLayout/SplitLayout';
import { TextInput } from '../components/TextInput/TextInput';
import { useSignUpMutation } from '../hooks/useQueries';
import { useAuthentication } from '../hooks/useAuthentication';
import { useNavigate } from 'react-router-dom';
import { Routing } from '../lib/routing';

export const signUpSchema = Yup.object({
  email: Yup.string().email('Invalid email address').required('Email is required.'),
  password: Yup.string()
    .min(6, 'Must be 6 or more characters')
    .max(20, 'Must be 20 characters or less')
    .required('Password is required.'),
  firstname: Yup.string().required('Firstname is required.'),
  lastname: Yup.string().required('Lastname is required.'),
  company: Yup.string().required('Company is required.'),
});

export const SignUpPage: React.FC = () => {
  const navigate = useNavigate();
  const { login, loading } = useAuthentication();
  const signUpMutation = useSignUpMutation();
  const formik = useFormik({
    initialValues: {
      email: '',
      password: '',
      firstname: '',
      lastname: '',
      company: '',
    },
    validationSchema: signUpSchema,
    onSubmit: (values) => {
      signUpMutation
        .mutateAsync({ ...values, username: '' })
        .then((_account) => login({ email: values.email, password: values.password }));
      formik.setSubmitting(false);
    },
  });
  return (
    <SplitLayout
      left={
        <form onSubmit={formik.handleSubmit}>
          <img
            className="h-12 w-auto"
            src={process.env.REACT_APP_LOGO_URL}
            alt={process.env.REACT_APP_BRAND_NAME}
          />
          <h2 className="mt-6 text-3xl font-bold tracking-tight text-gray-900">
            Create your account
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            or{' '}
            <span
              className="font-medium text-indigo-600 hover:text-indigo-500 cursor-pointer"
              onClick={() => navigate(Routing.getSignInRoute())}
            >
              have an account?
            </span>
          </p>
          <div className="mt-6">
            <TextInput
              id="email"
              name="email"
              type="email"
              label="Email address"
              isValid={formik.touched.email && !formik.errors.email}
              validationError={formik.errors.email}
              value={formik.values.email}
              disabled={signUpMutation.isLoading || loading}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
            />
          </div>
          <div className="mt-6">
            <TextInput
              id="password"
              name="password"
              type="password"
              label="Password"
              value={formik.values.password}
              isValid={formik.touched.password && !formik.errors.password}
              validationError={formik.errors.password}
              required
              disabled={signUpMutation.isLoading || loading}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
            />
          </div>
          <div className="mt-6 grid grid-cols-1 gap-y-6 gap-x-4 sm:grid-cols-6">
            <div className="sm:col-span-3">
              <TextInput
                id="firstname"
                name="firstname"
                type="text"
                label="First name"
                isValid={formik.touched.firstname && !formik.errors.firstname}
                validationError={formik.errors.firstname}
                value={formik.values.firstname}
                disabled={signUpMutation.isLoading || loading}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
              />
            </div>

            <div className="sm:col-span-3">
              <TextInput
                id="lastname"
                name="lastname"
                type="text"
                label="Last name"
                isValid={formik.touched.lastname && !formik.errors.lastname}
                validationError={formik.errors.lastname}
                value={formik.values.lastname}
                disabled={signUpMutation.isLoading || loading}
                onChange={formik.handleChange}
                onBlur={formik.handleBlur}
              />
            </div>
          </div>
          <div className="mt-6">
            <TextInput
              id="company"
              name="company"
              type="text"
              label="Company"
              value={formik.values.company}
              isValid={formik.touched.company && !formik.errors.company}
              validationError={formik.errors.company}
              required
              disabled={signUpMutation.isLoading || loading}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
            />
          </div>
          <div className="mt-6">
            <Button
              type="submit"
              fullWidth
              disabled={
                !formik.isValid ||
                formik.isSubmitting ||
                !formik.dirty ||
                signUpMutation.isLoading ||
                loading
              }
            >
              Register
            </Button>
          </div>
        </form>
      }
      right={
        <img
          className="absolute inset-0 h-full w-full object-cover"
          src={process.env.REACT_APP_SIGIN_BACKGROUND_IMAGE}
          alt="sign in background"
        />
      }
    />
  );
};
