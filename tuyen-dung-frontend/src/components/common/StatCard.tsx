import s from './StatCard.module.css';

interface Props {
  icon: string;
  label: string;
  value: string | number;
  sub?: string;
  trend?: 'up' | 'down' | 'neutral';
  trendValue?: string;
  accent?: 'indigo' | 'sky' | 'amber' | 'emerald' | 'rose';
}

const ACCENT_CLASS: Record<NonNullable<Props['accent']>, string> = {
  indigo: s.indigo,
  sky: s.sky,
  amber: s.amber,
  emerald: s.emerald,
  rose: s.rose,
};

const TREND_ICON: Record<NonNullable<Props['trend']>, string> = {
  up: '↑',
  down: '↓',
  neutral: '→',
};

/**
 * StatCard — pure presentational component (SRP + DIP: depends on Props interface).
 * Reused across all 3 dashboard pages.
 */
export default function StatCard({
  icon,
  label,
  value,
  sub,
  trend = 'neutral',
  trendValue,
  accent = 'indigo',
}: Props) {
  return (
    <div className={s.card}>
      <div className={`${s.iconWrap} ${ACCENT_CLASS[accent]}`}>{icon}</div>
      <div className={s.body}>
        <div className={s.label}>{label}</div>
        <div className={s.value}>{value}</div>
        {(sub || trendValue) && (
          <div className={s.footer}>
            {trendValue && (
              <span className={`${s.trend} ${s[trend]}`}>
                {TREND_ICON[trend]} {trendValue}
              </span>
            )}
            {sub && <span className={s.sub}>{sub}</span>}
          </div>
        )}
      </div>
    </div>
  );
}
