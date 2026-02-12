export type TreeNode<T> = T & { children?: TreeNode<T>[] };

export function buildTree<T extends { id: number; parentId?: number | null }>(items: T[]): TreeNode<T>[] {
    const map = new Map<number, TreeNode<T>>();
    const roots: TreeNode<T>[] = [];
    for (const item of items) {
        if (item == null || item.id == null) {
            continue;
        }
        map.set(item.id, {...item, children: []});
    }
    for (const node of map.values()) {
        const parentId = node.parentId ?? null;
        if (parentId != null && map.has(parentId)) {
            map.get(parentId)!.children!.push(node);
        } else {
            roots.push(node);
        }
    }
    return roots;
}
