import {listMenus, listUsers, type MenuVO, searchUsers, type UserVO} from "../../api/system";

let cachedMenus: MenuVO[] | null = null;
let menusLoading: Promise<MenuVO[]> | null = null;

let cachedUsers: UserVO[] | null = null;
let usersLoading: Promise<UserVO[]> | null = null;
const userSearchCache = new Map<string, UserVO[]>();
const userSearchLoading = new Map<string, Promise<UserVO[]>>();

export async function loadDataScopeMenus(): Promise<MenuVO[]> {
    if (cachedMenus) {
        return cachedMenus;
    }
    if (menusLoading) {
        return menusLoading;
    }
    menusLoading = (async () => {
        try {
            const result = await listMenus();
            if (result?.code === 200 && result.data) {
                cachedMenus = result.data.filter((menu) => !!menu.permission);
                return cachedMenus;
            }
        } finally {
            menusLoading = null;
        }
        cachedMenus = [];
        return cachedMenus;
    })();
    return menusLoading;
}

export async function loadDataScopeUsers(): Promise<UserVO[]> {
    if (cachedUsers) {
        return cachedUsers;
    }
    if (usersLoading) {
        return usersLoading;
    }
    usersLoading = (async () => {
        try {
            const result = await listUsers({pageNum: 1, pageSize: 200});
            if (result?.code === 200 && result.data) {
                cachedUsers = result.data.data;
                return cachedUsers;
            }
        } finally {
            usersLoading = null;
        }
        cachedUsers = [];
        return cachedUsers;
    })();
    return usersLoading;
}

export async function searchDataScopeUsers(query: string): Promise<UserVO[]> {
    const key = query.trim();
    if (!key) {
        return loadDataScopeUsers();
    }
    const cached = userSearchCache.get(key);
    if (cached) {
        return cached;
    }
    const inflight = userSearchLoading.get(key);
    if (inflight) {
        return inflight;
    }
    const task = (async () => {
        try {
            const result = await searchUsers({pageNum: 1, pageSize: 200, keyword: key});
            if (result?.code === 200 && result.data) {
                const users = result.data.data;
                userSearchCache.set(key, users);
                return users;
            }
        } finally {
            userSearchLoading.delete(key);
        }
        userSearchCache.set(key, []);
        return [];
    })();
    userSearchLoading.set(key, task);
    return task;
}
