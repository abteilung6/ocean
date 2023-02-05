import React, { DetailedHTMLProps, InputHTMLAttributes } from 'react';

export type HTMLInputProps = DetailedHTMLProps<
  InputHTMLAttributes<HTMLInputElement>,
  HTMLInputElement
>;

export type TextInputProps = HTMLInputProps & {
  label?: string;
  isValid?: boolean;
  validationError?: string;
};

export const TextInput: React.FC<TextInputProps> = ({
  value,
  label,
  id,
  type = 'text',
  disabled = false,
  name,
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
        <label htmlFor={id ?? name} className={'block text-sm font-medium text-gray-700'}>
          {label}
        </label>
      )}
      <div className="mt-1">
        <input
          type={type}
          id={id ?? name}
          name={name}
          className={[
            'block w-full rounded-md shadow-sm',
            'disabled:cursor-not-allowed disabled:border-gray-200 disabled:bg-gray-50 disabled:text-gray-500',
            colors,
          ].join(' ')}
          value={value}
          disabled={disabled}
          {...props}
        />
      </div>
      {!isValid && validationError && (
        <p className="mt-1 text-sm text-red-600" id="email-error">
          {validationError}
        </p>
      )}
    </div>
  );
};
