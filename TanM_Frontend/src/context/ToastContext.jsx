import React, { createContext, useContext, useState, useCallback, useEffect, useRef } from 'react';

const ToastContext = createContext(null);

// Global event bus for non-hook usage (e.g. from API handlers or outside React components)
const toastEvents = {
  listeners: new Set(),
  emit(toast) {
    this.listeners.forEach((fn) => fn(toast));
  },
  subscribe(fn) {
    this.listeners.add(fn);
    return () => this.listeners.delete(fn);
  },
};

export const showToast = (message, type = 'info', duration = 4000) => {
  toastEvents.emit({
    id: `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`,
    message,
    type,
    duration,
  });
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    // Return fallback helpers that still emit via the event bus if used outside provider
    return {
      success: (msg, dur) => showToast(msg, 'success', dur),
      error: (msg, dur) => showToast(msg, 'error', dur || 5000),
      warning: (msg, dur) => showToast(msg, 'warning', dur || 4500),
      info: (msg, dur) => showToast(msg, 'info', dur),
      dismiss: () => {},
    };
  }
  return context;
};

const TOAST_ICONS = {
  success: { name: 'check_circle', color: 'text-emerald-400', border: 'border-l-emerald-500', bar: 'bg-emerald-500' },
  error: { name: 'error', color: 'text-rose-400', border: 'border-l-rose-500', bar: 'bg-rose-500' },
  warning: { name: 'warning', color: 'text-amber-400', border: 'border-l-amber-500', bar: 'bg-amber-500' },
  info: { name: 'info', color: 'text-neutral-300', border: 'border-l-neutral-400', bar: 'bg-neutral-400' },
};

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback((toast) => {
    setToasts((prev) => [...prev.slice(-4), toast]); // Keep max 5 active toasts
  }, []);

  useEffect(() => {
    const unsubscribe = toastEvents.subscribe(addToast);
    return unsubscribe;
  }, [addToast]);

  const success = useCallback((message, duration = 4000) => {
    showToast(message, 'success', duration);
  }, []);

  const error = useCallback((message, duration = 5000) => {
    showToast(message, 'error', duration);
  }, []);

  const warning = useCallback((message, duration = 4500) => {
    showToast(message, 'warning', duration);
  }, []);

  const info = useCallback((message, duration = 4000) => {
    showToast(message, 'info', duration);
  }, []);

  return (
    <ToastContext.Provider value={{ success, error, warning, info, dismiss }}>
      {children}
      <ToastContainer toasts={toasts} onDismiss={dismiss} />
    </ToastContext.Provider>
  );
}

function ToastContainer({ toasts, onDismiss }) {
  if (toasts.length === 0) return null;

  return (
    <div
      aria-live="polite"
      className="fixed top-6 right-6 z-[99999] flex flex-col gap-3 max-w-sm w-[calc(100vw-3rem)] sm:w-full pointer-events-none"
    >
      {toasts.map((toast) => (
        <ToastItem key={toast.id} toast={toast} onDismiss={onDismiss} />
      ))}
    </div>
  );
}

function ToastItem({ toast, onDismiss }) {
  const [isExiting, setIsExiting] = useState(false);
  const timerRef = useRef(null);
  const iconConfig = TOAST_ICONS[toast.type] || TOAST_ICONS.info;

  const handleClose = () => {
    setIsExiting(true);
    setTimeout(() => onDismiss(toast.id), 250);
  };

  useEffect(() => {
    timerRef.current = setTimeout(() => {
      handleClose();
    }, toast.duration);

    return () => clearTimeout(timerRef.current);
  }, [toast.duration]);

  return (
    <div
      role="alert"
      className={`pointer-events-auto relative overflow-hidden flex items-start gap-3 p-4 
        bg-[#111111]/95 text-white backdrop-blur-md border border-white/10 rounded-sm shadow-2xl
        border-l-4 ${iconConfig.border}
        transition-all duration-300 transform
        ${isExiting ? 'opacity-0 translate-x-12 scale-95' : 'opacity-100 translate-x-0 scale-100 animate-toast-slide-in'}
      `}
    >
      <span className={`material-symbols-outlined text-[20px] shrink-0 mt-0.5 ${iconConfig.color}`}>
        {iconConfig.name}
      </span>

      <div className="flex-1 min-w-0 pr-2">
        <p className="text-xs font-medium leading-relaxed tracking-wide text-neutral-100 select-text break-words">
          {toast.message}
        </p>
      </div>

      <button
        type="button"
        onClick={handleClose}
        className="text-neutral-400 hover:text-white transition-colors shrink-0 cursor-pointer p-0.5"
        aria-label="Dismiss notification"
      >
        <span className="material-symbols-outlined text-[16px]">close</span>
      </button>

      {/* Progress countdown line */}
      <div
        className={`absolute bottom-0 left-0 h-[2px] ${iconConfig.bar} opacity-60`}
        style={{
          width: '100%',
          animation: `toastCountdown ${toast.duration}ms linear forwards`,
        }}
      />
    </div>
  );
}
