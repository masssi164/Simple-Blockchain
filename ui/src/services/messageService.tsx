import toast from 'react-hot-toast';
import { CheckBadgeIcon, ExclamationTriangleIcon } from '@heroicons/react/24/solid';

export const messageService = {
  success(msg: string) {
    toast.custom(
      t => (
        <div
          className={`${t.visible ? 'animate-enter' : 'animate-leave'} pointer-events-auto flex w-full max-w-sm rounded-lg bg-slate-800 p-4 text-white shadow-lg ring-1 ring-black ring-opacity-5`}
          role="status"
        >
          <CheckBadgeIcon className="h-6 w-6 text-green-400" aria-hidden="true" />
          <div className="ml-3 flex-1 text-sm">{msg}</div>
        </div>
      ),
      { duration: 4000 },
    );
  },
  error(msg: string) {
    toast.custom(
      t => (
        <div
          className={`${t.visible ? 'animate-enter' : 'animate-leave'} pointer-events-auto flex w-full max-w-sm rounded-lg bg-red-600/90 p-4 text-white shadow-lg ring-1 ring-black ring-opacity-5`}
          role="alert"
        >
          <ExclamationTriangleIcon className="h-6 w-6" aria-hidden="true" />
          <div className="ml-3 flex-1 text-sm">{msg}</div>
        </div>
      ),
      { duration: 4000 },
    );
  },
};
