import React from 'react';
import { classNames } from '../../lib/stringUtils';

export interface NavbarProps {
  paths: ReadonlyArray<{ name: string; route: string }>;
  selectedRoute?: string;
  onClick?: (route: string) => void;
}

export const Navbar: React.FC<NavbarProps> = ({ paths, selectedRoute, onClick }) => {
  console.log(selectedRoute);

  return (
    <nav className="bg-white shadow-sm">
      <div className="mx-auto  px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 justify-between">
          <div className="flex">
            <div className="flex flex-shrink-0 items-center">
              <img
                className="h-8 w-auto"
                src={process.env.REACT_APP_LOGO_URL}
                alt={process.env.REACT_APP_BRAND_NAME}
              />
            </div>
            <div className="hidden sm:-my-px sm:ml-6 sm:flex sm:space-x-8">
              {paths.map((item) => (
                <div
                  key={item.name}
                  className={classNames(
                    item.route === selectedRoute
                      ? 'border-indigo-500 text-gray-900'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
                    'inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium cursor-pointer'
                  )}
                  aria-current={item.route === selectedRoute ? 'page' : undefined}
                  onClick={() => onClick?.(item.route)}
                >
                  {item.name}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};
