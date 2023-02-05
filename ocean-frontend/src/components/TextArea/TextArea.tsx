import React, { DetailedHTMLProps, TextareaHTMLAttributes } from 'react';

export type TextAreaInputProps = DetailedHTMLProps<
  TextareaHTMLAttributes<HTMLTextAreaElement>,
  HTMLTextAreaElement
>;

export type TextAreaProps = TextAreaInputProps & {
  label?: string;
  isValid?: boolean;
  validationError?: string;
};

export const TextArea: React.FC<TextAreaProps> = ({
  id,
  name,
  value,
  disabled = false,
  className = '',
  label,
  isValid = true,
  validationError,
  ...props
}) => {
  const colors = isValid
    ? 'rounded-md border-gray-300 focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm'
    : 'rounded-md border-red-300 text-red-900 placeholder-red-300 focus:border-red-500 focus:outline-none focus:ring-red-500 sm:text-sm';

  return (
    <div>
      {label && (
        <label htmlFor={id ?? name} className="block text-sm font-medium text-gray-700">
          {label}
        </label>
      )}
      <div className="mt-1">
        <textarea
          id={id ?? name}
          name={name}
          rows={4}
          className={['block w-full rounded-md shadow-sm sm:text-sm', colors, className].join(' ')}
          {...props}
        />
      </div>
      {!isValid && validationError && (
        <p className="mt-2 text-sm text-red-600" id="email-error">
          {validationError}
        </p>
      )}
    </div>
  );
};
