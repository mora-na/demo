export function rafThrottle<T extends (...args: any[]) => void>(fn: T): T {
    let rafId: number | null = null;
    let lastArgs: any[] | null = null;

    const throttled = (...args: any[]) => {
        lastArgs = args;
        if (rafId == null) {
            rafId = requestAnimationFrame(() => {
                fn(...(lastArgs || []));
                rafId = null;
                lastArgs = null;
            });
        }
    };

    return throttled as T;
}
