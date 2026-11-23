export default function StatCard({ label, value, icon: Icon, note }) {
  return (
    <article className="stat-card">
      <div className="stat-top">
        <span>{label}</span>
        <span className="stat-icon"><Icon size={21} /></span>
      </div>
      <strong>{value}</strong>
      <small>{note}</small>
    </article>
  );
}
