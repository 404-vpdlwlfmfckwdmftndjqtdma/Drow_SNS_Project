"use client";

import { useCallback, useState } from "react";
import type { BlurRange } from "@/types";

export function useBlurRanges(): BlurRange[] {
  return [];
}

export function useBlurActions() {
  const [, setRangesState] = useState<BlurRange[]>([]);

  const setRanges = useCallback((ranges: BlurRange[]) => {
    setRangesState(ranges);
  }, []);

  const addRange = useCallback((range: BlurRange) => {
    setRangesState((current) => mergeRanges([...current, range]));
  }, []);

  const removeRange = useCallback((index: number) => {
    setRangesState((current) => current.filter((_, i) => i !== index));
  }, []);

  const clear = useCallback(() => {
    setRangesState([]);
  }, []);

  return { setRanges, addRange, removeRange, clear };
}

function mergeRanges(ranges: BlurRange[]): BlurRange[] {
  const sorted = [...ranges].sort((a, b) => a.start - b.start);
  const merged: BlurRange[] = [];

  for (const range of sorted) {
    const last = merged[merged.length - 1];
    if (last && range.start <= last.end) {
      last.end = Math.max(last.end, range.end);
    } else {
      merged.push({ ...range });
    }
  }

  return merged;
}
