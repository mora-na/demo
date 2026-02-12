import {sm2} from "sm-crypto";

type BufferLike = {
    from: (data: string | Uint8Array, encoding?: string) => Uint8Array & { toString: (encoding: string) => string };
};

const textEncoder = new TextEncoder();
const nodeBuffer = (globalThis as { Buffer?: BufferLike }).Buffer;

function base64ToBytes(base64: string): Uint8Array {
    if (typeof atob === "function") {
        const binary = atob(base64);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i += 1) {
            bytes[i] = binary.charCodeAt(i);
        }
        return bytes;
    }
    if (nodeBuffer) {
        return Uint8Array.from(nodeBuffer.from(base64, "base64"));
    }
    throw new Error("Base64 decoder is not available");
}

function bytesToBase64(bytes: Uint8Array): string {
    if (typeof btoa === "function") {
        let binary = "";
        for (let i = 0; i < bytes.length; i += 1) {
            binary += String.fromCharCode(bytes[i]);
        }
        return btoa(binary);
    }
    if (nodeBuffer) {
        return nodeBuffer.from(bytes).toString("base64");
    }
    throw new Error("Base64 encoder is not available");
}

function bytesToHex(bytes: Uint8Array): string {
    let hex = "";
    for (let i = 0; i < bytes.length; i += 1) {
        const value = bytes[i].toString(16).padStart(2, "0");
        hex += value;
    }
    return hex;
}

function hexToBytes(hex: string): Uint8Array {
    const normalized = hex.length % 2 === 0 ? hex : `0${hex}`;
    const bytes = new Uint8Array(normalized.length / 2);
    for (let i = 0; i < normalized.length; i += 2) {
        bytes[i / 2] = Number.parseInt(normalized.slice(i, i + 2), 16);
    }
    return bytes;
}

function readDerLength(bytes: Uint8Array, offset: number): { length: number; offset: number } {
    const first = bytes[offset];
    if (first < 0x80) {
        return {length: first, offset: offset + 1};
    }
    const count = first & 0x7f;
    let length = 0;
    for (let i = 1; i <= count; i += 1) {
        length = (length << 8) | bytes[offset + i];
    }
    return {length, offset: offset + 1 + count};
}

function spkiBase64ToPublicKeyHex(base64Key?: string): string | null {
    if (!base64Key) {
        return null;
    }
    const bytes = base64ToBytes(base64Key);
    let offset = 0;
    if (bytes[offset] !== 0x30) {
        return null;
    }
    offset += 1;
    const seqLen = readDerLength(bytes, offset);
    offset = seqLen.offset;
    const seqEnd = offset + seqLen.length;
    if (bytes[offset] !== 0x30) {
        return null;
    }
    offset += 1;
    const algLen = readDerLength(bytes, offset);
    offset = algLen.offset + algLen.length;
    if (bytes[offset] !== 0x03) {
        return null;
    }
    offset += 1;
    const bitLen = readDerLength(bytes, offset);
    offset = bitLen.offset;
    if (offset >= seqEnd) {
        return null;
    }
    const unusedBits = bytes[offset];
    if (unusedBits !== 0x00) {
        return null;
    }
    offset += 1;
    const keyBytes = bytes.slice(offset, offset + bitLen.length - 1);
    return bytesToHex(keyBytes);
}

function normalizeSm2PublicKey(hexKey?: string | null): string | null {
    if (!hexKey) {
        return null;
    }
    return hexKey.startsWith("04") ? hexKey : `04${hexKey}`;
}

function encryptSm2(plainText: string, base64PublicKey?: string): string {
    const keyHex = normalizeSm2PublicKey(spkiBase64ToPublicKeyHex(base64PublicKey));
    if (!keyHex) {
        throw new Error("SM2 public key is missing or invalid");
    }
    const cipherHex = sm2.doEncrypt(plainText, keyHex, 1);
    const normalized = `04${cipherHex}`;
    return bytesToBase64(hexToBytes(normalized));
}

async function encryptAesGcm(plainText: string, base64Key?: string): Promise<string> {
    if (!base64Key) {
        throw new Error("AES-GCM key is missing");
    }
    const crypto = globalThis.crypto;
    if (!crypto?.subtle) {
        throw new Error("Web Crypto API is not available");
    }
    const keyBytes = base64ToBytes(base64Key);
    const keyMaterial = new Uint8Array(keyBytes);
    const iv = crypto.getRandomValues(new Uint8Array(12));
    const key = await crypto.subtle.importKey("raw", keyMaterial, "AES-GCM", false, ["encrypt"]);
    const cipherBuffer = await crypto.subtle.encrypt(
        {name: "AES-GCM", iv},
        key,
        textEncoder.encode(plainText)
    );
    const cipherBytes = new Uint8Array(cipherBuffer);
    return `${bytesToBase64(iv)}:${bytesToBase64(cipherBytes)}`;
}

export async function encodeTransportPassword(plainText: string): Promise<string> {
    const mode = (import.meta.env.VITE_PASSWORD_TRANSPORT_MODE || "plain").toLowerCase();
    switch (mode) {
        case "plain":
            return plainText;
        case "base64":
            return bytesToBase64(textEncoder.encode(plainText));
        case "aes":
        case "aes-gcm":
            return encryptAesGcm(plainText, import.meta.env.VITE_PASSWORD_TRANSPORT_KEY);
        case "sm2":
            return encryptSm2(plainText, import.meta.env.VITE_PASSWORD_TRANSPORT_SM2_PUBLIC_KEY);
        default:
            return plainText;
    }
}
