import api from "./http";
import {encodeTransportPassword} from "../utils/passwordTransport";

export async function fetchCaptcha() {
  const response = await api.get("/auth/captcha");
  return response.data;
}

export async function login(payload) {
  const encrypted = await encodeTransportPassword(payload.password);
  const response = await api.post("/auth/login", {
    userName: payload.userName,
    password: encrypted,
    captchaId: payload.captchaId,
    captchaCode: payload.captchaCode
  });
  return response.data;
}
