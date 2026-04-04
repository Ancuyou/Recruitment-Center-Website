import { useCallback, useEffect, useState } from 'react';

type DraftHistoryState<T> = {
  past: T[];
  present: T;
  future: T[];
};

type SetValueArg<T> = T | ((prev: T) => T);

interface UseDraftHistoryOptions<T> {
  storageKey: string;
  initialValue: T;
  maxHistory?: number;
}

function isSerializableEqual<T>(left: T, right: T): boolean {
  try {
    return JSON.stringify(left) === JSON.stringify(right);
  } catch {
    return false;
  }
}

function createDefaultState<T>(initialValue: T): DraftHistoryState<T> {
  return {
    past: [],
    present: initialValue,
    future: [],
  };
}

function readStoredState<T>(storageKey: string, initialValue: T): DraftHistoryState<T> {
  if (typeof window === 'undefined') {
    return createDefaultState(initialValue);
  }

  try {
    const raw = window.sessionStorage.getItem(storageKey);
    if (!raw) return createDefaultState(initialValue);

    const parsed = JSON.parse(raw) as DraftHistoryState<T>;
    if (!parsed || !Array.isArray(parsed.past) || !Array.isArray(parsed.future)) {
      return createDefaultState(initialValue);
    }

    return parsed;
  } catch {
    return createDefaultState(initialValue);
  }
}

function writeStoredState<T>(storageKey: string, state: DraftHistoryState<T>): void {
  if (typeof window === 'undefined') return;

  try {
    window.sessionStorage.setItem(storageKey, JSON.stringify(state));
  } catch {
    // ignore storage write errors (private mode/quota)
  }
}

export function useDraftHistory<T>({
  storageKey,
  initialValue,
  maxHistory = 30,
}: UseDraftHistoryOptions<T>) {
  const [state, setState] = useState<DraftHistoryState<T>>(() =>
    readStoredState(storageKey, initialValue)
  );

  useEffect(() => {
    setState(readStoredState(storageKey, initialValue));
  }, [storageKey, initialValue]);

  useEffect(() => {
    writeStoredState(storageKey, state);
  }, [storageKey, state]);

  const setValue = useCallback(
    (arg: SetValueArg<T>) => {
      setState((prev) => {
        const next = typeof arg === 'function' ? (arg as (current: T) => T)(prev.present) : arg;

        if (isSerializableEqual(prev.present, next)) {
          return prev;
        }

        const nextPast = [...prev.past, prev.present];
        if (nextPast.length > maxHistory) {
          nextPast.shift();
        }

        return {
          past: nextPast,
          present: next,
          future: [],
        };
      });
    },
    [maxHistory]
  );

  const replaceValue = useCallback((next: T) => {
    setState({
      past: [],
      present: next,
      future: [],
    });
  }, []);

  const undo = useCallback(() => {
    setState((prev) => {
      if (prev.past.length === 0) return prev;

      const previousValue = prev.past[prev.past.length - 1];
      const nextFuture = [prev.present, ...prev.future];
      if (nextFuture.length > maxHistory) {
        nextFuture.pop();
      }

      return {
        past: prev.past.slice(0, -1),
        present: previousValue,
        future: nextFuture,
      };
    });
  }, [maxHistory]);

  const redo = useCallback(() => {
    setState((prev) => {
      if (prev.future.length === 0) return prev;

      const nextValue = prev.future[0];
      const nextPast = [...prev.past, prev.present];
      if (nextPast.length > maxHistory) {
        nextPast.shift();
      }

      return {
        past: nextPast,
        present: nextValue,
        future: prev.future.slice(1),
      };
    });
  }, [maxHistory]);

  const clearDraft = useCallback(
    (nextValue?: T) => {
      if (typeof window !== 'undefined') {
        try {
          window.sessionStorage.removeItem(storageKey);
        } catch {
          // ignore storage delete errors
        }
      }

      setState(createDefaultState(nextValue ?? initialValue));
    },
    [initialValue, storageKey]
  );

  return {
    value: state.present,
    setValue,
    replaceValue,
    undo,
    redo,
    clearDraft,
    canUndo: state.past.length > 0,
    canRedo: state.future.length > 0,
  };
}
