import api from "./http";
import {encodeTransportPassword} from "../utils/passwordTransport";

export interface ApiResponse<T> {
    code: number;
    message?: string;
    data?: T;
}

export interface PageResult<T> {
    total: number;
    data: T[];
    pageNum: number;
    pageSize: number;
}

export interface UserVO {
    id: number;
    userName: string;
    nickName: string;
    phone?: string;
    email?: string;
    sex?: string;
    status?: number;
    deptId?: number | null;
    dataScopeType?: string;
    dataScopeValue?: string;
    remark?: string;
}

export interface RoleVO {
    id: number;
    code: string;
    name: string;
    status?: number;
    dataScopeType?: string;
    dataScopeValue?: string;
    permissionIds?: number[];
}

export interface RoleMenuDataScopeItem {
    menuId: number;
    menuName?: string;
    parentId?: number | null;
    permission?: string;
    dataScopeType?: string | null;
    customDeptIds?: number[];
}

export interface RoleMenuDataScopeResponse {
    roleId: number;
    roleCode?: string;
    roleName?: string;
    defaultDataScopeType?: string;
    defaultDataScopeValue?: string;
    items: RoleMenuDataScopeItem[];
}

export interface RoleMenuDataScopeItemPayload {
    menuId: number;
    dataScopeType?: string | null;
    customDeptIds?: number[];
}

export interface UserDataScopeVO {
    id: number;
    userId: number;
    userName?: string;
    nickName?: string;
    deptId?: number | null;
    deptName?: string;
    scopeKey?: string;
    menuName?: string;
    permission?: string;
    dataScopeType?: string;
    dataScopeValue?: string;
    status?: number;
    remark?: string;
    createTime?: string;
}

export interface UserDataScopeDetailResponse {
    userId: number;
    userName?: string;
    nickName?: string;
    deptId?: number | null;
    deptName?: string;
    overrides: UserDataScopeVO[];
}

export interface UserDataScopeCreatePayload {
    scopeKey: string;
    dataScopeType: string;
    dataScopeValue?: string;
    status?: number;
    remark?: string;
}

export interface UserDataScopeUpdatePayload {
    dataScopeType?: string;
    dataScopeValue?: string;
    status?: number;
    remark?: string;
}

export interface UserDataScopeQuery {
    pageNum?: number;
    pageSize?: number;
    userName?: string;
    menuKeyword?: string;
    status?: number;
}

export interface DataScopeRuleVO {
    id: number;
    scopeKey: string;
    tableName: string;
    tableAlias?: string;
    deptColumn?: string | null;
    userColumn?: string | null;
    filterType?: number;
    status?: number;
    remark?: string;
}

export interface DataScopeRuleCreatePayload {
    scopeKey: string;
    tableName: string;
    tableAlias?: string;
    deptColumn?: string | null;
    userColumn?: string | null;
    filterType?: number;
    status?: number;
    remark?: string;
}

export interface DataScopeRuleUpdatePayload {
    scopeKey?: string;
    tableName?: string;
    tableAlias?: string;
    deptColumn?: string | null;
    userColumn?: string | null;
    filterType?: number;
    status?: number;
    remark?: string;
}

export interface DataScopeRuleQuery {
    pageNum?: number;
    pageSize?: number;
    scopeKey?: string;
    tableName?: string;
}

export interface DataScopeResolveResponse {
    user?: {
        id: number;
        userName?: string;
        nickName?: string;
        deptId?: number | null;
        deptName?: string;
        posts?: string[];
        roles?: Array<{
            id: number;
            code?: string;
            name?: string;
            dataScopeType?: string;
            dataScopeValue?: string;
        }>;
    };
    menu?: {
        id?: number;
        name?: string;
        permission?: string;
    };
    layer3?: {
        scopeKey?: string;
        dataScopeType?: string;
        dataScopeValue?: string;
    };
    roleScopes?: Array<{
        roleId?: number;
        roleCode?: string;
        roleName?: string;
        layer1Type?: string;
        layer2Type?: string;
        effectiveType?: string;
        sourceLayer?: string;
        customDeptIds?: number[];
    }>;
    mergedDeptIds?: number[];
    includeSelf?: boolean;
    finalScopeLabel?: string;
    rule?: {
        source?: string;
        tableName?: string;
        tableAlias?: string;
        deptColumn?: string | null;
        userColumn?: string | null;
    };
    sqlCondition?: string;
}

export interface DataScopeResolveMenuVO {
    menuId: number;
    menuName?: string;
    permission?: string;
    finalScopeLabel?: string;
    sourceLayer?: string;
}

export interface PermissionVO {
    id: number;
    code: string;
    name: string;
    status?: number;
}

export interface MenuVO {
    id: number;
    name: string;
    code?: string;
    parentId?: number | null;
    path?: string;
    component?: string;
    permission?: string;
    status?: number;
    sort?: number;
    remark?: string;
}

export interface DeptVO {
    id: number;
    name: string;
    code?: string;
    parentId?: number | null;
    status?: number;
    sort?: number;
    remark?: string;
}

export interface PostVO {
    id: number;
    name: string;
    code?: string;
    deptId?: number | null;
    status?: number;
    sort?: number;
    remark?: string;
}

export interface OperLog {
    id: number;
    userId?: number | null;
    userName?: string;
    deptId?: number | null;
    deptName?: string;
    title?: string;
    operation?: string;
    businessType?: number;
    method?: string;
    requestMethod?: string;
    operUrl?: string;
    operIp?: string;
    operLocation?: string;
    operParam?: string;
    operResult?: string;
    beforeData?: string;
    afterData?: string;
    status?: number;
    errorMsg?: string;
    costTime?: number;
    operTime?: string;
}

export interface LoginLog {
    id: number;
    userId?: number | null;
    userName?: string;
    loginIp?: string;
    loginLocation?: string;
    browser?: string;
    os?: string;
    deviceType?: string;
    loginType?: number;
    status?: number;
    msg?: string;
    loginTime?: string;
}

export interface OperLogQuery {
    pageNum?: number;
    pageSize?: number;
    userName?: string;
    title?: string;
    status?: number;
    businessType?: number;
    beginTime?: string;
    endTime?: string;
}

export interface LoginLogQuery {
    pageNum?: number;
    pageSize?: number;
    userName?: string;
    loginIp?: string;
    status?: number;
    loginType?: number;
    beginTime?: string;
    endTime?: string;
}

export interface UserQuery {
    pageNum?: number;
    pageSize?: number;
    userName?: string;
    nickName?: string;
    phone?: string;
    email?: string;
    sex?: string;
    status?: number;
    deptId?: number | null;
}

export interface UserSearchQuery {
    keyword?: string;
    pageNum?: number;
    pageSize?: number;
}

export interface UserCreatePayload {
    userName: string;
    nickName?: string;
    phone?: string;
    email?: string;
    password?: string;
    sex?: string;
    status?: number;
    deptId?: number | null;
    dataScopeType?: string;
    dataScopeValue?: string;
    remark?: string;
}

export interface UserUpdatePayload {
    userName?: string;
    nickName?: string;
    phone?: string;
    email?: string;
    sex?: string;
    status?: number;
    deptId?: number | null;
    dataScopeType?: string;
    dataScopeValue?: string;
    remark?: string;
}

export interface RoleCreatePayload {
    code: string;
    name: string;
    status?: number;
    dataScopeType?: string;
    dataScopeValue?: string;
}

export interface RoleUpdatePayload {
    code: string;
    name: string;
    dataScopeType?: string;
    dataScopeValue?: string;
}

export interface PermissionCreatePayload {
    code: string;
    name: string;
    status?: number;
}

export interface PermissionUpdatePayload {
    code: string;
    name: string;
}

export interface MenuCreatePayload {
    name: string;
    code?: string;
    parentId?: number | null;
    path?: string;
    component?: string;
    permission?: string;
    status?: number;
    sort?: number;
    remark?: string;
}

export interface MenuUpdatePayload {
    name?: string;
    code?: string;
    parentId?: number | null;
    path?: string;
    component?: string;
    permission?: string;
    status?: number;
    sort?: number;
    remark?: string;
}

export interface DeptCreatePayload {
    name: string;
    code?: string;
    parentId?: number | null;
    status?: number;
    sort?: number;
    remark?: string;
}

export interface DeptUpdatePayload {
    name?: string;
    code?: string;
    parentId?: number | null;
    status?: number;
    sort?: number;
    remark?: string;
}

export interface PostCreatePayload {
    name: string;
    code?: string;
    deptId: number;
    status?: number;
    sort?: number;
    remark?: string;
}

export interface PostUpdatePayload {
    name?: string;
    code?: string;
    deptId?: number | null;
    status?: number;
    sort?: number;
    remark?: string;
}

export async function listUsers(params: UserQuery): Promise<ApiResponse<PageResult<UserVO>>> {
    const response = await api.get<ApiResponse<PageResult<UserVO>>>("/users", {params});
    return response.data;
}

export async function searchUsers(params: UserSearchQuery): Promise<ApiResponse<PageResult<UserVO>>> {
    const response = await api.get<ApiResponse<PageResult<UserVO>>>("/users/search", {params});
    return response.data;
}

export async function getUserDetail(id: number): Promise<ApiResponse<UserVO>> {
    const response = await api.get<ApiResponse<UserVO>>(`/users/${id}`);
    return response.data;
}

export async function getUserRoleIds(id: number): Promise<ApiResponse<number[]>> {
    const response = await api.get<ApiResponse<number[]>>(`/users/${id}/roles`);
    return response.data;
}

export async function getUserPostIds(id: number): Promise<ApiResponse<number[]>> {
    const response = await api.get<ApiResponse<number[]>>(`/users/${id}/posts`);
    return response.data;
}

export async function createUser(payload: UserCreatePayload): Promise<ApiResponse<UserVO>> {
    const request = {...payload};
    if (request.password) {
        request.password = await encodeTransportPassword(request.password);
    }
    const response = await api.post<ApiResponse<UserVO>>("/users", request);
    return response.data;
}

export async function updateUser(id: number, payload: UserUpdatePayload): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/users/${id}`, payload);
    return response.data;
}

export async function updateUserStatus(id: number, status: number): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/users/${id}/status`, {status});
    return response.data;
}

export async function deleteUser(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/users/${id}`);
    return response.data;
}

export async function deleteUsers(ids: number[]): Promise<ApiResponse<void>> {
    const response = await api.post<ApiResponse<void>>("/users/batch-delete", ids);
    return response.data;
}

export async function resetUserPassword(id: number, newPassword: string): Promise<ApiResponse<void>> {
    const encoded = await encodeTransportPassword(newPassword);
    const response = await api.put<ApiResponse<void>>(`/users/${id}/reset-password`, {newPassword: encoded});
    return response.data;
}

export async function assignUserRoles(id: number, roleIds: number[]): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/users/${id}/roles`, {roleIds});
    return response.data;
}

export async function assignUserPosts(id: number, postIds: number[]): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/users/${id}/posts`, {postIds});
    return response.data;
}

export async function getUserDataScopeDetail(id: number): Promise<ApiResponse<UserDataScopeDetailResponse>> {
    const response = await api.get<ApiResponse<UserDataScopeDetailResponse>>(`/users/${id}/data-scope`);
    return response.data;
}

export async function createUserDataScope(id: number, payload: UserDataScopeCreatePayload): Promise<ApiResponse<UserDataScopeVO>> {
    const response = await api.post<ApiResponse<UserDataScopeVO>>(`/users/${id}/data-scope`, payload);
    return response.data;
}

export async function updateUserDataScope(scopeId: number, payload: UserDataScopeUpdatePayload): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/users/data-scope/${scopeId}`, payload);
    return response.data;
}

export async function deleteUserDataScope(scopeId: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/users/data-scope/${scopeId}`);
    return response.data;
}

export async function listUserDataScopes(params: UserDataScopeQuery): Promise<ApiResponse<PageResult<UserDataScopeVO>>> {
    const response = await api.get<ApiResponse<PageResult<UserDataScopeVO>>>(`/user-data-scope/list`, {params});
    return response.data;
}

export async function listRoles(): Promise<ApiResponse<RoleVO[]>> {
    const response = await api.get<ApiResponse<RoleVO[]>>("/roles");
    return response.data;
}

export async function createRole(payload: RoleCreatePayload): Promise<ApiResponse<RoleVO>> {
    const response = await api.post<ApiResponse<RoleVO>>("/roles", payload);
    return response.data;
}

export async function updateRole(id: number, payload: RoleUpdatePayload): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/roles/${id}`, payload);
    return response.data;
}

export async function updateRoleStatus(id: number, status: number): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/roles/${id}/status`, {status});
    return response.data;
}

export async function deleteRole(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/roles/${id}`);
    return response.data;
}

export async function deleteRoles(ids: number[]): Promise<ApiResponse<void>> {
    const response = await api.post<ApiResponse<void>>("/roles/batch-delete", ids);
    return response.data;
}

export async function assignRolePermissions(id: number, permissionIds: number[]): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/roles/${id}/permissions`, {permissionIds});
    return response.data;
}

export async function getRoleMenuIds(id: number): Promise<ApiResponse<number[]>> {
    const response = await api.get<ApiResponse<number[]>>(`/roles/${id}/menus`);
    return response.data;
}

export async function assignRoleMenus(id: number, menuIds: number[]): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/roles/${id}/menus`, {menuIds});
    return response.data;
}

export async function listOperLogs(params: OperLogQuery): Promise<ApiResponse<PageResult<OperLog>>> {
    const response = await api.get<ApiResponse<PageResult<OperLog>>>("/logs/oper", {params});
    return response.data;
}

export async function listLoginLogs(params: LoginLogQuery): Promise<ApiResponse<PageResult<LoginLog>>> {
    const response = await api.get<ApiResponse<PageResult<LoginLog>>>("/logs/login", {params});
    return response.data;
}

export async function deleteOperLog(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/logs/oper/${id}`);
    return response.data;
}

export async function deleteLoginLog(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/logs/login/${id}`);
    return response.data;
}

export async function getRoleMenuDataScope(id: number): Promise<ApiResponse<RoleMenuDataScopeResponse>> {
    const response = await api.get<ApiResponse<RoleMenuDataScopeResponse>>(`/roles/${id}/menu-data-scope`);
    return response.data;
}

export async function saveRoleMenuDataScope(id: number, items: RoleMenuDataScopeItemPayload[]): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/roles/${id}/menu-data-scope`, {items});
    return response.data;
}

export async function clearRoleMenuDataScope(roleId: number, menuId: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/roles/${roleId}/menu-data-scope/${menuId}`);
    return response.data;
}

export async function listPermissions(): Promise<ApiResponse<PermissionVO[]>> {
    const response = await api.get<ApiResponse<PermissionVO[]>>("/permissions");
    return response.data;
}

export async function createPermission(payload: PermissionCreatePayload): Promise<ApiResponse<PermissionVO>> {
    const response = await api.post<ApiResponse<PermissionVO>>("/permissions", payload);
    return response.data;
}

export async function updatePermission(id: number, payload: PermissionUpdatePayload): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/permissions/${id}`, payload);
    return response.data;
}

export async function updatePermissionStatus(id: number, status: number): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/permissions/${id}/status`, {status});
    return response.data;
}

export async function deletePermission(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/permissions/${id}`);
    return response.data;
}

export async function deletePermissions(ids: number[]): Promise<ApiResponse<void>> {
    const response = await api.post<ApiResponse<void>>("/permissions/batch-delete", ids);
    return response.data;
}

export async function listMenus(): Promise<ApiResponse<MenuVO[]>> {
    const response = await api.get<ApiResponse<MenuVO[]>>("/menus");
    return response.data;
}

export async function createMenu(payload: MenuCreatePayload): Promise<ApiResponse<MenuVO>> {
    const response = await api.post<ApiResponse<MenuVO>>("/menus", payload);
    return response.data;
}

export async function updateMenu(id: number, payload: MenuUpdatePayload): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/menus/${id}`, payload);
    return response.data;
}

export async function updateMenuStatus(id: number, status: number): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/menus/${id}/status`, {status});
    return response.data;
}

export async function deleteMenu(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/menus/${id}`);
    return response.data;
}

export async function deleteMenus(ids: number[]): Promise<ApiResponse<void>> {
    const response = await api.post<ApiResponse<void>>("/menus/batch-delete", ids);
    return response.data;
}

export async function listDepts(): Promise<ApiResponse<DeptVO[]>> {
    const response = await api.get<ApiResponse<DeptVO[]>>("/depts");
    return response.data;
}

export async function createDept(payload: DeptCreatePayload): Promise<ApiResponse<DeptVO>> {
    const response = await api.post<ApiResponse<DeptVO>>("/depts", payload);
    return response.data;
}

export async function updateDept(id: number, payload: DeptUpdatePayload): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/depts/${id}`, payload);
    return response.data;
}

export async function updateDeptStatus(id: number, status: number): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/depts/${id}/status`, {status});
    return response.data;
}

export async function deleteDept(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/depts/${id}`);
    return response.data;
}

export async function deleteDepts(ids: number[]): Promise<ApiResponse<void>> {
    const response = await api.post<ApiResponse<void>>("/depts/batch-delete", ids);
    return response.data;
}

export async function listPosts(): Promise<ApiResponse<PostVO[]>> {
    const response = await api.get<ApiResponse<PostVO[]>>("/posts");
    return response.data;
}

export async function createPost(payload: PostCreatePayload): Promise<ApiResponse<PostVO>> {
    const response = await api.post<ApiResponse<PostVO>>("/posts", payload);
    return response.data;
}

export async function updatePost(id: number, payload: PostUpdatePayload): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/posts/${id}`, payload);
    return response.data;
}

export async function updatePostStatus(id: number, status: number): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/posts/${id}/status`, {status});
    return response.data;
}

export async function deletePost(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/posts/${id}`);
    return response.data;
}

export async function deletePosts(ids: number[]): Promise<ApiResponse<void>> {
    const response = await api.post<ApiResponse<void>>("/posts/batch-delete", ids);
    return response.data;
}

export interface NoticeVO {
    id: number;
    title: string;
    content: string;
    scopeType?: string;
    scopeValue?: string | null;
    createdBy?: number | null;
    createdName?: string;
    createdAt?: string;
    totalCount?: number;
    readCount?: number;
}

export interface NoticeRecipientVO {
    id: number;
    userId: number;
    userName?: string;
    nickName?: string;
    deptId?: number | null;
    readStatus?: number;
    readTime?: string;
}

export interface NoticeMyVO {
    id: number;
    title: string;
    content: string;
    createdName?: string;
    createdAt?: string;
    readStatus?: number;
    readTime?: string;
}

export interface NoticeStreamMetrics {
    totalConnections: number;
    activeUsers: number;
    latestCacheSize: number;
    connectionCounterSize: number;
    latestLimit: number;
    maxTotalConnections: number;
    maxConnectionsPerUser: number;
    latestCacheMaxSize: number;
    latestCacheExpireMinutes: number;
    autoDegradeEnabled: boolean;
    degraded: boolean;
    degradeConnectionRatio: number;
    degradeCacheRatio: number;
}

export interface NoticePublishPayload {
    title: string;
    content: string;
    scopeType: string;
    scopeIds?: number[];
}

export interface NoticeQuery {
    pageNum?: number;
    pageSize?: number;
    keyword?: string;
    scopeType?: string;
}

export async function listNotices(params: NoticeQuery): Promise<ApiResponse<PageResult<NoticeVO>>> {
    const response = await api.get<ApiResponse<PageResult<NoticeVO>>>('/notices', {params});
    return response.data;
}

export async function deleteNotice(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/notices/${id}`);
    return response.data;
}

export async function deleteNotices(ids: number[]): Promise<ApiResponse<void>> {
    const response = await api.post<ApiResponse<void>>("/notices/batch-delete", ids);
    return response.data;
}

export async function publishNotice(payload: NoticePublishPayload): Promise<ApiResponse<NoticeVO>> {
    const response = await api.post<ApiResponse<NoticeVO>>('/notices', payload);
    return response.data;
}

export async function getNoticeDetail(id: number): Promise<ApiResponse<NoticeVO>> {
    const response = await api.get<ApiResponse<NoticeVO>>(`/notices/${id}`);
    return response.data;
}

export async function listNoticeRecipients(id: number): Promise<ApiResponse<NoticeRecipientVO[]>> {
    const response = await api.get<ApiResponse<NoticeRecipientVO[]>>(`/notices/${id}/recipients`);
    return response.data;
}

export async function listMyNotices(params: {
    pageNum?: number;
    pageSize?: number
}): Promise<ApiResponse<PageResult<NoticeMyVO>>> {
    const response = await api.get<ApiResponse<PageResult<NoticeMyVO>>>('/notices/my', {params});
    return response.data;
}

export async function getUnreadNoticeCount(): Promise<ApiResponse<number>> {
    const response = await api.get<ApiResponse<number>>('/notices/unread-count');
    return response.data;
}

export async function markNoticeRead(id: number): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/notices/${id}/read`);
    return response.data;
}

export async function markAllNoticesRead(): Promise<ApiResponse<number>> {
    const response = await api.put<ApiResponse<number>>('/notices/read-all');
    return response.data;
}

export async function getNoticeStreamMetrics(): Promise<ApiResponse<NoticeStreamMetrics>> {
    const response = await api.get<ApiResponse<NoticeStreamMetrics>>('/notices/stream/metrics');
    return response.data;
}

export interface JobVO {
    id: number;
    name: string;
    handlerName: string;
    cronExpression: string;
    status?: number;
    allowConcurrent?: number;
    misfirePolicy?: string;
    params?: string;
    logCollectLevel?: string;
    remark?: string;
    createdName?: string;
    createdAt?: string;
    updatedAt?: string;
    nextFireTime?: string;
}

export interface JobHandlerInfo {
    name: string;
    className: string;
}

export interface JobQuery {
    pageNum?: number;
    pageSize?: number;
    name?: string;
    handlerName?: string;
    status?: number;
}

export interface JobCreatePayload {
    name: string;
    handlerName: string;
    cronExpression: string;
    status?: number;
    allowConcurrent?: number;
    misfirePolicy?: string;
    params?: string;
    logCollectLevel?: string;
    remark?: string;
}

export interface JobUpdatePayload {
    name?: string;
    handlerName?: string;
    cronExpression?: string;
    status?: number;
    allowConcurrent?: number;
    misfirePolicy?: string;
    params?: string;
    logCollectLevel?: string;
    remark?: string;
}

export interface JobLogVO {
    id: number;
    jobId: number;
    jobName: string;
    handlerName: string;
    status?: number;
    message?: string;
    startTime?: string;
    endTime?: string;
    durationMs?: number;
}

export interface JobLogDetailVO extends JobLogVO {
    logDetail?: string;
}

export interface JobLogCollectorMetrics {
    enabled: boolean;
    autoDegradeEnabled: boolean;
    degraded: boolean;
    bufferSize: number;
    maxBuffers: number;
    maxLength: number;
    maxHoldMillis: number;
    mergeDelayMillis: number;
    degradeBufferRatio: number;
}

export interface JobCronPreviewVO {
    cronExpression: string;
    timeZone?: string;
    nextFireTimes: string[];
}

export async function listJobs(params: JobQuery): Promise<ApiResponse<PageResult<JobVO>>> {
    const response = await api.get<ApiResponse<PageResult<JobVO>>>('/jobs', {params});
    return response.data;
}

export async function getJobDetail(id: number): Promise<ApiResponse<JobVO>> {
    const response = await api.get<ApiResponse<JobVO>>(`/jobs/${id}`);
    return response.data;
}

export async function createJob(payload: JobCreatePayload): Promise<ApiResponse<JobVO>> {
    const response = await api.post<ApiResponse<JobVO>>('/jobs', payload);
    return response.data;
}

export async function updateJob(id: number, payload: JobUpdatePayload): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/jobs/${id}`, payload);
    return response.data;
}

export async function deleteJob(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/jobs/${id}`);
    return response.data;
}

export async function updateJobStatus(id: number, status: number): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/jobs/${id}/status`, {status});
    return response.data;
}

export async function runJob(id: number): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/jobs/${id}/run`);
    return response.data;
}

export async function listJobHandlers(): Promise<ApiResponse<JobHandlerInfo[]>> {
    const response = await api.get<ApiResponse<JobHandlerInfo[]>>('/jobs/handlers');
    return response.data;
}

export async function previewJobCron(cronExpression: string): Promise<ApiResponse<JobCronPreviewVO>> {
    const response = await api.post<ApiResponse<JobCronPreviewVO>>('/jobs/cron/preview', {cronExpression});
    return response.data;
}

export async function listJobLogs(id: number, params: {
    pageNum?: number;
    pageSize?: number
}): Promise<ApiResponse<PageResult<JobLogVO>>> {
    const response = await api.get<ApiResponse<PageResult<JobLogVO>>>(`/jobs/${id}/logs`, {params});
    return response.data;
}

export async function getJobLogDetail(logId: number): Promise<ApiResponse<JobLogDetailVO>> {
    const response = await api.get<ApiResponse<JobLogDetailVO>>(`/jobs/logs/${logId}`);
    return response.data;
}

export async function getJobLogCollectorMetrics(): Promise<ApiResponse<JobLogCollectorMetrics>> {
    const response = await api.get<ApiResponse<JobLogCollectorMetrics>>('/jobs/logs/metrics');
    return response.data;
}

export async function listDataScopeRules(params: DataScopeRuleQuery): Promise<ApiResponse<PageResult<DataScopeRuleVO>>> {
    const response = await api.get<ApiResponse<PageResult<DataScopeRuleVO>>>(`/data-scope-mapping/list`, {params});
    return response.data;
}

export async function createDataScopeRule(payload: DataScopeRuleCreatePayload): Promise<ApiResponse<DataScopeRuleVO>> {
    const response = await api.post<ApiResponse<DataScopeRuleVO>>(`/data-scope-mapping`, payload);
    return response.data;
}

export async function updateDataScopeRule(id: number, payload: DataScopeRuleUpdatePayload): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/data-scope-mapping/${id}`, payload);
    return response.data;
}

export async function deleteDataScopeRule(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/data-scope-mapping/${id}`);
    return response.data;
}

export async function resolveDataScope(userId: number, permission?: string): Promise<ApiResponse<DataScopeResolveResponse>> {
    const response = await api.get<ApiResponse<DataScopeResolveResponse>>(`/data-scope/resolve`, {
        params: {userId, permission}
    });
    return response.data;
}

export async function resolveAllDataScope(userId: number): Promise<ApiResponse<DataScopeResolveMenuVO[]>> {
    const response = await api.get<ApiResponse<DataScopeResolveMenuVO[]>>(`/data-scope/resolve-all`, {
        params: {userId}
    });
    return response.data;
}

export interface DictTypeVO {
    id: number;
    dictType: string;
    dictName: string;
    status?: number;
    sort?: number;
    remark?: string;
    createTime?: string;
}

export interface DictDataVO {
    id: number;
    dictType: string;
    dictLabel: string;
    dictValue: string;
    status?: number;
    sort?: number;
    remark?: string;
    createTime?: string;
}

export interface DictTypeQuery {
    pageNum?: number;
    pageSize?: number;
    dictType?: string;
    dictName?: string;
    status?: number;
}

export interface DictDataQuery {
    pageNum?: number;
    pageSize?: number;
    dictType?: string;
    dictLabel?: string;
    dictValue?: string;
    status?: number;
}

export interface DictTypeCreatePayload {
    dictType: string;
    dictName: string;
    status?: number;
    sort?: number;
    remark?: string;
}

export interface DictTypeUpdatePayload {
    dictType: string;
    dictName: string;
    status?: number;
    sort?: number;
    remark?: string;
}

export interface DictDataCreatePayload {
    dictType: string;
    dictLabel: string;
    dictValue: string;
    status?: number;
    sort?: number;
    remark?: string;
}

export interface DictDataUpdatePayload {
    dictLabel: string;
    dictValue: string;
    status?: number;
    sort?: number;
    remark?: string;
}

export async function listDictTypes(params: DictTypeQuery): Promise<ApiResponse<PageResult<DictTypeVO>>> {
    const response = await api.get<ApiResponse<PageResult<DictTypeVO>>>(`/sys/dict/type/list`, {params});
    return response.data;
}

export async function createDictType(payload: DictTypeCreatePayload): Promise<ApiResponse<DictTypeVO>> {
    const response = await api.post<ApiResponse<DictTypeVO>>(`/sys/dict/type`, payload);
    return response.data;
}

export async function updateDictType(id: number, payload: DictTypeUpdatePayload): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/sys/dict/type/${id}`, payload);
    return response.data;
}

export async function deleteDictType(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/sys/dict/type/${id}`);
    return response.data;
}

export async function listDictData(params: DictDataQuery): Promise<ApiResponse<PageResult<DictDataVO>>> {
    const response = await api.get<ApiResponse<PageResult<DictDataVO>>>(`/sys/dict/data/list`, {params});
    return response.data;
}

export async function createDictData(payload: DictDataCreatePayload): Promise<ApiResponse<DictDataVO>> {
    const response = await api.post<ApiResponse<DictDataVO>>(`/sys/dict/data`, payload);
    return response.data;
}

export async function updateDictData(id: number, payload: DictDataUpdatePayload): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/sys/dict/data/${id}`, payload);
    return response.data;
}

export async function deleteDictData(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/sys/dict/data/${id}`);
    return response.data;
}

export async function refreshDictCache(): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/sys/dict/cache/refresh`);
    return response.data;
}

export async function fetchDictDataByType(dictType: string): Promise<ApiResponse<DictDataVO[]>> {
    const response = await api.get<ApiResponse<DictDataVO[]>>(`/dict/data/${dictType}`);
    return response.data;
}

export async function fetchDictDataBatch(types: string[]): Promise<ApiResponse<Record<string, DictDataVO[]>>> {
    const response = await api.get<ApiResponse<Record<string, DictDataVO[]>>>(`/dict/data/batch`, {
        params: {types: types.join(",")}
    });
    return response.data;
}

export async function fetchAllDictData(): Promise<ApiResponse<Record<string, DictDataVO[]>>> {
    const response = await api.get<ApiResponse<Record<string, DictDataVO[]>>>(`/dict/data/all`);
    return response.data;
}
