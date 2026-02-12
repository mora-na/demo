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
    sex?: string;
    status?: number;
    deptId?: number | null;
    dataScopeType?: string;
    dataScopeValue?: string;
    tst?: string;
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

export interface UserQuery {
    pageNum?: number;
    pageSize?: number;
    userName?: string;
    nickName?: string;
    sex?: string;
    status?: number;
    deptId?: number | null;
}

export interface UserCreatePayload {
    userName: string;
    nickName?: string;
    password?: string;
    sex?: string;
    status?: number;
    deptId?: number | null;
    dataScopeType?: string;
    dataScopeValue?: string;
    tst?: string;
}

export interface UserUpdatePayload {
    userName?: string;
    nickName?: string;
    sex?: string;
    status?: number;
    deptId?: number | null;
    dataScopeType?: string;
    dataScopeValue?: string;
    tst?: string;
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

export async function listUsers(params: UserQuery): Promise<ApiResponse<PageResult<UserVO>>> {
    const response = await api.get<ApiResponse<PageResult<UserVO>>>("/users", {params});
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

export async function resetUserPassword(id: number, newPassword: string): Promise<ApiResponse<void>> {
    const encoded = await encodeTransportPassword(newPassword);
    const response = await api.put<ApiResponse<void>>(`/users/${id}/reset-password`, {newPassword: encoded});
    return response.data;
}

export async function assignUserRoles(id: number, roleIds: number[]): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/users/${id}/roles`, {roleIds});
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

export interface JobVO {
    id: number;
    name: string;
    handlerName: string;
    cronExpression: string;
    status?: number;
    allowConcurrent?: number;
    misfirePolicy?: string;
    targetType?: string;
    targetIds?: number[];
    params?: string;
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
    targetType?: string;
    targetIds?: number[];
    params?: string;
    remark?: string;
}

export interface JobUpdatePayload {
    name?: string;
    handlerName?: string;
    cronExpression?: string;
    status?: number;
    allowConcurrent?: number;
    misfirePolicy?: string;
    targetType?: string;
    targetIds?: number[];
    params?: string;
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

export async function listJobLogs(id: number, params: {
    pageNum?: number;
    pageSize?: number
}): Promise<ApiResponse<PageResult<JobLogVO>>> {
    const response = await api.get<ApiResponse<PageResult<JobLogVO>>>(`/jobs/${id}/logs`, {params});
    return response.data;
}
