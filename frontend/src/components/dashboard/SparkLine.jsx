/**
 * SparkLine.jsx
 * SRP: dibuja una mini línea de tendencia con SVG.
 * No hace fetch — solo visualización decorativa.
 */

const PATHS = {
  orange: 'M0,20 C10,18 20,8 30,10 C40,12 50,5 60,8 C70,11 80,4 90,6',
  green:  'M0,18 C10,15 20,12 30,14 C40,16 50,8  60,6 C70,4  80,2  90,4',
  blue:   'M0,15 C15,13 25,10 35,12 C45,14 55,9  65,7 C75,5  85,8  90,6',
  red:    'M0,8  C10,10 20,14 30,12 C40,10 50,16 60,14 C70,12 80,18 90,16',
};

const COLORS = {
  orange: '#F58232',
  green:  '#10B981',
  blue:   '#3B82F6',
  red:    '#EF4444',
};

export default function SparkLine({ color = 'orange' }) {
  const d   = PATHS[color]  ?? PATHS.orange;
  const clr = COLORS[color] ?? COLORS.orange;

  return (
    <svg
      className="stat-sparkline"
      viewBox="0 0 90 24"
      fill="none"
      aria-hidden="true"
      preserveAspectRatio="none"
    >
      <path d={d} stroke={clr} strokeWidth="2" strokeLinecap="round" opacity="0.6" />
    </svg>
  );
}
