import api from "./http";
import {encodeTransportPassword} from "../utils/passwordTransport";

export interface CaptchaData {
    captchaId: string;
    imageBase64: string;
}

export interface LoginPayload {
    userName: string;
    password: string;
    captchaId: string;
    captchaCode: string;
}

export interface LoginData {
    token: string;
    tokenType?: string;
    expiresAt?: number;
    passwordChangeRequired?: boolean;
    passwordExpired?: boolean;
    firstLoginForceChange?: boolean;
    passwordExpireDays?: number;
}

export interface UpdateProfilePayload {
    nickName?: string;
    phone?: string;
    email?: string;
    sex?: string;
    remark?: string;
    oldPassword?: string;
    newPassword?: string;
}

export interface UserProfileInfo {
    id: number;
    userName: string;
    nickName: string;
    phone?: string;
    email?: string;
    sex?: string;
    deptId: number | null;
    deptName?: string;
    dataScopeType: string;
    dataScopeValue: string;
}

export interface UserRoleTarget {
    id: number;
    code?: string;
    name?: string;
}

export interface MenuTree {
    id: number;
    name: string;
    code: string;
    parentId: number | null;
    path: string;
    component: string;
    permission: string;
    status: number;
    sort: number;
    remark: string;
    children?: MenuTree[];
}

export interface UserProfileResponse {
    user: UserProfileInfo;
    roles: string[];
    roleTargets?: UserRoleTarget[];
    permissions: string[];
    menus: MenuTree[];
    passwordChangeRequired?: boolean;
    passwordExpired?: boolean;
    firstLoginForceChange?: boolean;
}

export interface LogoutPayload {
    token?: string;
}

export interface ApiResponse<T> {
    code: number;
    message?: string;
    data?: T;
}

export async function fetchCaptcha(): Promise<ApiResponse<CaptchaData>> {
    const response = await api.get<ApiResponse<CaptchaData>>("/auth/captcha");
    return response.data;
}

export async function login(payload: LoginPayload): Promise<ApiResponse<LoginData>> {
    const encrypted = await encodeTransportPassword(payload.password);
    const response = await api.post<ApiResponse<LoginData>>("/auth/login", {
        userName: payload.userName,
        password: encrypted,
        captchaId: payload.captchaId,
        captchaCode: payload.captchaCode
    });
    return response.data;
}

export async function logout(token?: string): Promise<ApiResponse<void>> {
    const headers = token ? {Authorization: `Bearer ${token}`} : undefined;
    const response = await api.post<ApiResponse<void>>("/auth/logout", {token}, {headers});
    return response.data;
}

export async function fetchProfile(): Promise<ApiResponse<UserProfileResponse>> {
    const response = await api.get<ApiResponse<UserProfileResponse>>("/auth/profile");
    return response.data;
}

export async function updateProfile(payload: UpdateProfilePayload): Promise<ApiResponse<void>> {
    const request = {...payload};
    if (payload.oldPassword) {
        request.oldPassword = await encodeTransportPassword(payload.oldPassword);
    }
    if (payload.newPassword) {
        request.newPassword = await encodeTransportPassword(payload.newPassword);
    }
    const response = await api.put<ApiResponse<void>>("/auth/profile", request);
    return response.data;
}
