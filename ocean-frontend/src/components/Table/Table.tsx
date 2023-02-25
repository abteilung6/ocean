import React from 'react';

export const Table: React.FC<{ children?: React.ReactNode }> = ({ children }) => {
  return (
    <table className="min-w-full divide-y divide-gray-300 overflow-hidden shadow ring-1 ring-black ring-opacity-5 rounded-lg">
      {children}
    </table>
  );
};

export const TableHead: React.FC<{ children?: React.ReactNode }> = ({ children }) => {
  return <thead className="bg-gray-50">{children}</thead>;
};

export const TableRow: React.FC<{ children?: React.ReactNode }> = ({ children }) => {
  return <tr>{children}</tr>;
};

export const HeaderCell: React.FC<{
  children?: React.ReactNode;
  textAlignment?: 'text-left' | 'text-center' | 'text-right';
}> = ({ children, textAlignment = 'text-left' }) => {
  return (
    <th
      scope="col"
      className={`px-3 py-3.5 text-left text-sm font-semibold text-gray-900 ${textAlignment} `}
    >
      {children}
    </th>
  );
};

export const TableBody: React.FC<{ children?: React.ReactNode }> = ({ children }) => {
  return <thead className="divide-y divide-gray-200 bg-white">{children}</thead>;
};

export const DataCell: React.FC<{
  children?: React.ReactNode;
  textAlignment?: 'text-left' | 'text-center' | 'text-right';
  onClick?: () => void;
}> = ({ children, textAlignment = 'text-left', onClick }) => {
  const clickableStyle = onClick ? 'hover:text-blue-600 hover:underline cursor-pointer' : '';
  return (
    <td className={['whitespace-nowrap px-3 py-4 text-sm text-gray-600', textAlignment].join(' ')}>
      <span className={[clickableStyle].join(' ')} onClick={onClick}>
        {children}
      </span>
    </td>
  );
};
