import {computed, type Ref, watch} from "vue";
import {useFormulaStore} from "../../stores/formulaStore";

export function useDropIndicator(canvasRef: Ref<HTMLElement | null>) {
    const store = useFormulaStore();

    const indicatorPosition = computed(() => {
        if (store.dragState.dropTargetIndex === null || !canvasRef.value) return null;

        const tokenElements = canvasRef.value.querySelectorAll<HTMLElement>("[data-token-index]");
        const index = store.dragState.dropTargetIndex;

        if (tokenElements.length === 0) {
            return {x: 16, y: 16};
        }

        if (index >= tokenElements.length) {
            const lastEl = tokenElements[tokenElements.length - 1];
            const rect = lastEl.getBoundingClientRect();
            const containerRect = canvasRef.value.getBoundingClientRect();
            return {
                x: rect.right - containerRect.left + 4,
                y: rect.top - containerRect.top
            };
        }

        const targetEl = tokenElements[index];
        const rect = targetEl.getBoundingClientRect();
        const containerRect = canvasRef.value.getBoundingClientRect();
        return {
            x: rect.left - containerRect.left - 2,
            y: rect.top - containerRect.top
        };
    });

    watch(
        () => store.dragState.dropTargetIndex,
        (newIndex) => {
            if (!canvasRef.value) return;
            const tokenElements = canvasRef.value.querySelectorAll<HTMLElement>("[data-token-index]");

            tokenElements.forEach((el) => {
                el.style.transition = "transform 0.2s ease";
                el.style.transform = "";
            });

            if (newIndex === null) return;
            const gap = 28;
            for (let i = newIndex; i < tokenElements.length; i += 1) {
                tokenElements[i].style.transform = `translateX(${gap}px)`;
            }
        }
    );

    return {
        indicatorPosition
    };
}
