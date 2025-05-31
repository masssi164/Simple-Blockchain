interface StatCardProps {
  label: string;
  value: string | number;
}

export const StatCard = ({ label, value }: StatCardProps) => (
  <div className="rounded-2xl bg-white shadow p-4 md:p-6 flex flex-col">
    <dt className="text-sm font-medium text-slate-500">{label}</dt>
    <dd
      className="mt-1 text-3xl font-semibold text-slate-900"
      aria-live="polite"
    >
      {value}
    </dd>
  </div>
);
