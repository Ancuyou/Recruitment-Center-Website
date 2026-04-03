import type { ReactNode } from 'react';
import s from './AppFormSection.module.css';

interface Props {
  title: string;
  description?: string;
  actions?: ReactNode;
  children: ReactNode;
}

export default function AppFormSection({ title, description, actions, children }: Props) {
  return (
    <section className={s.section}>
      <div className={s.head}>
        <div>
          <h3 className={s.title}>{title}</h3>
          {description ? <p className={s.description}>{description}</p> : null}
        </div>
        {actions}
      </div>
      <div className={s.body}>{children}</div>
    </section>
  );
}
