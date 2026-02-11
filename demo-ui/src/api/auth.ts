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
