"use client";

import { useSyncExternalStore } from "react";

let saveText = "";
const listeners = new Set<() => void>();

function notify() {
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function getSaveText() {
  return saveText;
}

export function setSaveText(text: string) {
  saveText = text;
  notify();
}

export function resetSaveText() {
  saveText = "";
  notify();
}

export function useSaveText() {
  const text = useSyncExternalStore(subscribe, getSaveText, getSaveText);
  return [text, setSaveText] as const;
}
