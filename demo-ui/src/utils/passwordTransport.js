import {sm2} from "sm-crypto";

const textEncoder = new TextEncoder();

function base64ToBytes(base64) {
  if (typeof atob === "function") {
    const binary = atob(base64);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i += 1) {
      bytes[i] = binary.charCodeAt(i);
    }
    return bytes;
  }
  return Uint8Array.from(Buffer.from(base64, "base64"));
}

function bytesToBase64(bytes) {
  if (typeof btoa === "function") {
    let binary = "";
    for (let i = 0; i < bytes.length; i += 1) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
  }
  return Buffer.from(bytes).toString("base64");
}

function bytesToHex(bytes) {
  let hex = "";
  for (let i = 0; i < bytes.length; i += 1) {
    const value = bytes[i].toString(16).padStart(2, "0");
    hex += value;
  }
  return hex;
}

function hexToBytes(hex) {
  const normalized = hex.length % 2 === 0 ? hex : `0${hex}`;
  const bytes = new Uint8Array(normalized.length / 2);
  for (let i = 0; i < normalized.length; i += 2) {
    bytes[i / 2] = Number.parseInt(normalized.slice(i, i + 2), 16);
  }
  return bytes;
}

function readDerLength(bytes, offset) {
  const first = bytes[offset];
  if (first < 0x80) {
    return { length: first, offset: offset + 1 };
  }
  const count = first & 0x7f;
  let length = 0;
  for (let i = 1; i <= count; i += 1) {
    length = (length << 8) | bytes[offset + i];
  }
  return { length, offset: offset + 1 + count };
}

function spkiBase64ToPublicKeyHex(base64Key) {
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

function normalizeSm2PublicKey(hexKey) {
  if (!hexKey) {
    return null;
  }
  return hexKey.startsWith("04") ? hexKey : `04${hexKey}`;
}

function encryptSm2(plainText, base64PublicKey) {
  const keyHex = normalizeSm2PublicKey(spkiBase64ToPublicKeyHex(base64PublicKey));
  if (!keyHex) {
    throw new Error("SM2 public key is missing or invalid");
  }
  const cipherHex = sm2.doEncrypt(plainText, keyHex, 1);
  const normalized = `04${cipherHex}`;
  return bytesToBase64(hexToBytes(normalized));
}

async function encryptAesGcm(plainText, base64Key) {
  if (!base64Key) {
    throw new Error("AES-GCM key is missing");
  }
  if (!globalThis.crypto?.subtle) {
    throw new Error("Web Crypto API is not available");
  }
  const keyBytes = base64ToBytes(base64Key);
  const iv = globalThis.crypto.getRandomValues(new Uint8Array(12));
  const key = await globalThis.crypto.subtle.importKey("raw", keyBytes, "AES-GCM", false, ["encrypt"]);
  const cipherBuffer = await globalThis.crypto.subtle.encrypt(
    { name: "AES-GCM", iv },
    key,
    textEncoder.encode(plainText)
  );
  const cipherBytes = new Uint8Array(cipherBuffer);
  return `${bytesToBase64(iv)}:${bytesToBase64(cipherBytes)}`;
}

export async function encodeTransportPassword(plainText) {
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
