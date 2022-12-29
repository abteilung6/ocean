import React, { ButtonHTMLAttributes, DetailedHTMLProps } from 'react';

type ButtonVariant = 'primary' | 'secondary' | 'white';

type ButtonSize = 'small' | 'medium' | 'large';

type ButtonProps = DetailedHTMLProps<ButtonHTMLAttributes<HTMLButtonElement>, HTMLButtonElement> & {
  variant?: ButtonVariant;
  size?: ButtonSize;
  disabled?: boolean;
  fullWidth?: boolean;
};

export const Button = ({
  variant = 'primary',
  size = 'medium',
  disabled = false,
  fullWidth = false,
  className,
  children,
  ...props
}: ButtonProps) => {
  const fullWidthStyle = fullWidth ? 'w-full' : '';
  return (
    <button
      type="button"
      className={[
        'inline-flex justify-center rounded-md border shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-75',
        sizeToStyles[size],
        variantToStyles[variant],
        fullWidthStyle,
        className,
      ].join(' ')}
      disabled={disabled}
      {...props}
    >
      {children}
    </button>
  );
};

const sizeToStyles: Record<ButtonSize, string> = {
  small: 'px-3 py-2 text-sm leading-4',
  medium: 'px-4 py-2 text-sm',
  large: 'px-4 py-2 text-base',
};

const variantToStyles: Record<ButtonVariant, string> = {
  primary: 'border-transparent bg-indigo-600 text-white hover:bg-indigo-700 focus:ring-indigo-500',
  secondary:
    'border-transparent bg-indigo-100 text-indigo-700 hover:bg-indigo-200 focus:ring-indigo-500',
  white: 'border-gray-300 text-gray-700 hover:bg-gray-50 focus:ring-indigo-500',
};
