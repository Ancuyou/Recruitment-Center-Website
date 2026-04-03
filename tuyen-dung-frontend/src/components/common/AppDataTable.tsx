import type { ReactNode } from 'react';
import s from './AppDataTable.module.css';

type Align = 'left' | 'center' | 'right';

export interface AppDataColumn<T> {
  key: keyof T | string;
  header: string;
  align?: Align;
  width?: string;
  render?: (row: T) => ReactNode;
}

interface Props<T> {
  columns: AppDataColumn<T>[];
  data: T[];
  emptyMessage?: string;
  rowKey: (row: T, index: number) => string;
}

export default function AppDataTable<T>({
  columns,
  data,
  emptyMessage = 'Không có dữ liệu.',
  rowKey,
}: Props<T>) {
  return (
    <div className={s.wrapper}>
      <table className={s.table}>
        <thead>
          <tr>
            {columns.map((column) => (
              <th
                key={String(column.key)}
                className={s.headCell}
                style={{ textAlign: column.align ?? 'left', width: column.width }}
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 ? (
            <tr>
              <td className={s.empty} colSpan={columns.length}>
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((row, index) => (
              <tr key={rowKey(row, index)} className={s.row}>
                {columns.map((column) => (
                  <td
                    key={String(column.key)}
                    className={s.cell}
                    style={{ textAlign: column.align ?? 'left' }}
                  >
                    {column.render ? column.render(row) : String((row as Record<string, unknown>)[String(column.key)] ?? '')}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
