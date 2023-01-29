import React from 'react';
import { XCircleIcon } from '@heroicons/react/20/solid';

export type AlertVariant = 'error';

export interface AlertProps {
  variant: AlertVariant;
  title?: string;
  description: string;
}

export const Alert: React.FC<AlertProps> = ({ variant, title, description }) => {
  return (
    <div className="rounded-md bg-red-50 p-4">
      <div className="flex">
        <div className="flex-shrink-0">
          <XCircleIcon className="h-5 w-5 text-red-400" aria-hidden="true" />
        </div>
        <div className="ml-3">
          <h3 className="text-sm font-medium text-red-800">
            {title ?? variantToTitleMapping[variant]}
          </h3>
          <div className="mt-2 text-sm text-red-700">
            <p>{description}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

const variantToTitleMapping: Record<AlertVariant, string> = {
  error: 'Error',
};
