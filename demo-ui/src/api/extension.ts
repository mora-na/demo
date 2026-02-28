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
    config?: string;
    status?: string;
    authMode?: string;
    rateLimitPolicy?: string;
    timeoutMs?: number;
    remark?: string;
    beanName?: string;
    paramMode?: string;
    paramSchema?: string;
    sql?: string;
    httpUrl?: string;
    httpMethod?: string;
    httpPassHeaders?: boolean;
    httpPassQuery?: boolean;
}

export interface DynamicApiBeanMethod {
    name: string;
    signature: string;
    parameterType?: string;
}

export interface DynamicApiBeanMeta {
    beanName: string;
    className: string;
    methods?: DynamicApiBeanMethod[];
}

export interface RateLimitPolicyMeta {
    id: string;
    name?: string;
    windowSeconds?: number;
    maxRequests?: number;
    keyMode?: string;
    includePath?: boolean;
}

export interface DynamicApiTypeMeta {
    code: string;
    name?: string;
}

export async function listDynamicApis(params: DynamicApiQuery): Promise<ApiResponse<PageResult<DynamicApi>>> {
    const response = await api.get<ApiResponse<PageResult<DynamicApi>>>("/dynamic-api", {params});
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

export async function listDynamicApiBeans(): Promise<ApiResponse<DynamicApiBeanMeta[]>> {
    const response = await api.get<ApiResponse<DynamicApiBeanMeta[]>>("/dynamic-api/metadata/beans");
    return response.data;
}

export async function listRateLimitPolicies(): Promise<ApiResponse<RateLimitPolicyMeta[]>> {
    const response = await api.get<ApiResponse<RateLimitPolicyMeta[]>>("/dynamic-api/metadata/rate-limit-policies");
    return response.data;
}

export async function listDynamicApiTypes(): Promise<ApiResponse<DynamicApiTypeMeta[]>> {
    const response = await api.get<ApiResponse<DynamicApiTypeMeta[]>>("/dynamic-api/metadata/types");
    return response.data;
}
