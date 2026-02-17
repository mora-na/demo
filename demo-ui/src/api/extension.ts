import api from "./http";
import type {ApiResponse, PageResult} from "./system";

export interface DynamicApi {
    id: number;
    path: string;
    method: string;
    status: string;
    type: string;
    config: string;
    authMode?: string;
    rateLimitPolicy?: string;
    timeoutMs?: number;
    remark?: string;
    createTime?: string;
    updateTime?: string;
}

export interface DynamicApiQuery {
    pageNum?: number;
    pageSize?: number;
    path?: string;
    method?: string;
    status?: string;
    type?: string;
    authMode?: string;
}

export interface DynamicApiPayload {
    path: string;
    method: string;
    type: string;
    config: string;
    status?: string;
    authMode?: string;
    rateLimitPolicy?: string;
    timeoutMs?: number;
    remark?: string;
}

export interface DynamicApiLog {
    id: number;
    apiId?: number;
    apiPath?: string;
    apiMethod?: string;
    apiType?: string;
    authMode?: string;
    status?: number;
    responseCode?: number;
    errorMsg?: string;
    traceId?: string;
    userId?: number;
    userName?: string;
    requestIp?: string;
    requestParam?: string;
    durationMs?: number;
    requestTime?: string;
}

export interface DynamicApiLogQuery {
    pageNum?: number;
    pageSize?: number;
    apiId?: number;
    apiPath?: string;
    apiMethod?: string;
    status?: number;
    userName?: string;
    beginTime?: string;
    endTime?: string;
}

export async function listDynamicApis(params: DynamicApiQuery): Promise<ApiResponse<PageResult<DynamicApi>>> {
    const response = await api.get<ApiResponse<PageResult<DynamicApi>>>("/dynamic-api", {params});
    return response.data;
}

export async function getDynamicApi(id: number): Promise<ApiResponse<DynamicApi>> {
    const response = await api.get<ApiResponse<DynamicApi>>(`/dynamic-api/${id}`);
    return response.data;
}

export async function createDynamicApi(payload: DynamicApiPayload): Promise<ApiResponse<DynamicApi>> {
    const response = await api.post<ApiResponse<DynamicApi>>("/dynamic-api", payload);
    return response.data;
}

export async function updateDynamicApi(id: number, payload: DynamicApiPayload): Promise<ApiResponse<DynamicApi>> {
    const response = await api.put<ApiResponse<DynamicApi>>(`/dynamic-api/${id}`, payload);
    return response.data;
}

export async function enableDynamicApi(id: number): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/dynamic-api/${id}/enable`);
    return response.data;
}

export async function disableDynamicApi(id: number): Promise<ApiResponse<void>> {
    const response = await api.put<ApiResponse<void>>(`/dynamic-api/${id}/disable`);
    return response.data;
}

export async function deleteDynamicApi(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/dynamic-api/${id}`);
    return response.data;
}

export async function reloadDynamicApis(): Promise<ApiResponse<void>> {
    const response = await api.post<ApiResponse<void>>("/dynamic-api/reload");
    return response.data;
}

export async function listDynamicApiLogs(params: DynamicApiLogQuery): Promise<ApiResponse<PageResult<DynamicApiLog>>> {
    const response = await api.get<ApiResponse<PageResult<DynamicApiLog>>>("/logs/dynamic-api", {params});
    return response.data;
}

export async function deleteDynamicApiLog(id: number): Promise<ApiResponse<void>> {
    const response = await api.delete<ApiResponse<void>>(`/logs/dynamic-api/${id}`);
    return response.data;
}

export async function batchDeleteDynamicApiLogs(ids: number[]): Promise<ApiResponse<void>> {
    const response = await api.post<ApiResponse<void>>("/logs/dynamic-api/batch-delete", ids);
    return response.data;
}
