import React from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { useNavigate } from 'react-router-dom';
import { useAuthentication } from '../hooks/useAuthentication';
import { Routing } from '../lib/routing';
import { Button } from '../components/Button/Button';
import { SplitLayout } from '../components/SplitLayout/SplitLayout';
import { TextInput } from '../components/TextInput/TextInput';
import { Alert } from '../components/Alert/Alert';

export const signInSchema = Yup.object({
  email: Yup.string().email('Invalid email address').required('Email is required.'),
  password: Yup.string()
    .min(6, 'Must be 8 or more characters')
    .max(20, 'Must be 20 characters or less')
    .required('Password is required'),
});

export const SignInPage: React.FC = () => {
  const navigate = useNavigate();
  const { error, login, loading } = useAuthentication();
  const formik = useFormik({
    initialValues: {
      email: '',
      password: '',
    },
    validationSchema: signInSchema,
    onSubmit: (values) => {
      login(values);
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
            Sign in to your account
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            or{' '}
            <span
              className="font-medium text-indigo-600 hover:text-indigo-500 cursor-pointer"
              onClick={() => navigate(Routing.getSignUpRoute())}
            >
              create your account
            </span>
          </p>
          <div className="mt-6">{error && <Alert variant="error" description={error} />}</div>
          <div className="mt-6">
            <TextInput
              id="email"
              name="email"
              type="email"
              label="Email address"
              isValid={formik.touched.email && !formik.errors.email}
              validationError={formik.errors.email}
              value={formik.values.email}
              disabled={loading}
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
              disabled={loading}
              onChange={formik.handleChange}
              onBlur={formik.handleBlur}
            />
          </div>
          <div className="mt-6">
            <Button
              type="submit"
              fullWidth
              disabled={!formik.isValid || formik.isSubmitting || !formik.dirty || loading}
            >
              Sign in
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
