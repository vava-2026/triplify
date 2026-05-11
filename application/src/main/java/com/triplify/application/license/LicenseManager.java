package com.triplify.application.license;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.time.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class LicenseManager {

    public enum CheckStatus { VALID, EXPIRED, MISSING, INVALID }

    public static class LicenseResult {
        public final boolean valid;
        public final String plan;
        public final String licenseId;
        public final String userEmail;
        public final Instant expiresAt;
        public final String errorMessage;

        private LicenseResult(boolean valid, String plan, String licenseId,
                              String userEmail, Instant expiresAt, String err) {
            this.valid = valid;
            this.plan = plan;
            this.licenseId = licenseId;
            this.userEmail = userEmail;
            this.expiresAt = expiresAt;
            this.errorMessage = err;
        }

        public static LicenseResult ok(String plan, String id, String email, Instant exp) {
            return new LicenseResult(true,  plan, id, email, exp, null);
        }
        public static LicenseResult fail(String reason) {
            return new LicenseResult(false, null, null, null, null, reason);
        }
    }

    public record LicenseCheckResult(CheckStatus status, LicenseResult license) {
    }

    private final PublicKey publicKey;
    private final Path licenseDir;


    public LicenseManager(String publicKeyPem, String appDataDir) throws Exception {
        this.publicKey = parsePublicKey(publicKeyPem);
        this.licenseDir = Path.of(appDataDir, "licenses");
        Files.createDirectories(this.licenseDir);
    }

    public LicenseResult installLicense(Path licenseFilePath, String userId) {
        try {
            String json = Files.readString(licenseFilePath);
            LicenseResult result = validateLicenseJson(json);
            if (!result.valid) return result;

            Path dest = licenseDir.resolve(sanitizeUserId(userId) + ".json");
            Files.writeString(dest, json);

            return result;
        } catch (Exception e) {
            return LicenseResult.fail("Failed to read license file: " + e.getMessage());
        }
    }

    public LicenseResult installLicenseFromJson(String json, String userId) {
        try {
            LicenseResult result = validateLicenseJson(json);
            if (!result.valid) return result;

            Path dest = licenseDir.resolve(sanitizeUserId(userId) + ".json");
            Files.writeString(dest, json);
            return result;
        } catch (Exception e) {
            return LicenseResult.fail("Failed to install license: " + e.getMessage());
        }
    }

    public LicenseCheckResult checkStoredLicense(String userId) {
        try {
            Path stored = licenseDir.resolve(sanitizeUserId(userId) + ".json");
            if (!Files.exists(stored)) {
                return new LicenseCheckResult(CheckStatus.MISSING, null);
            }

            String json = Files.readString(stored);
            LicenseResult result = validateLicenseJson(json);

            if (!result.valid) {
                return new LicenseCheckResult(CheckStatus.INVALID, null);
            }

            if (Instant.now().isAfter(result.expiresAt)) {
                return new LicenseCheckResult(CheckStatus.EXPIRED, result);
            }

            return new LicenseCheckResult(CheckStatus.VALID, result);

        } catch (Exception e) {
            return new LicenseCheckResult(CheckStatus.INVALID, null);
        }
    }

    public Optional<LicenseSummary> readLicenseSummary(String userId) {
        try {
            Path stored = licenseDir.resolve(sanitizeUserId(userId) + ".json");
            if (!Files.exists(stored)) return Optional.empty();
            String json = Files.readString(stored);
            Map<String, String> fields = parseJsonFields(json);
            return Optional.of(new LicenseSummary(
                    fields.get("licenseId"),
                    fields.get("userEmail"),
                    fields.get("plan"),
                    Instant.parse(fields.get("issuedAt")),
                    Instant.parse(fields.get("expiresAt"))
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static class LicenseSummary {
        public final String licenseId;
        public final String userEmail;
        public final String plan;
        public final Instant issuedAt;
        public final Instant expiresAt;

        public LicenseSummary(String id, String email, String plan, Instant issuedAt, Instant expiresAt) {
            this.licenseId = id;
            this.userEmail = email;
            this.plan = plan;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
        }

        public boolean isExpired() { return Instant.now().isAfter(expiresAt); }

        @Override public String toString() {
            return "LicenseSummary{id=" + licenseId + ", plan=" + plan + ", expires=" + expiresAt + "}";
        }
    }

    public boolean removeLicense(String userId) {
        try {
            Path stored = licenseDir.resolve(sanitizeUserId(userId) + ".json");
            return Files.deleteIfExists(stored);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isLicenseExpired(String userId) {
        try {
            Path stored = licenseDir.resolve(sanitizeUserId(userId) + ".json");
            if (!Files.exists(stored)) return true;
            Map<String, String> fields = parseJsonFields(Files.readString(stored));
            return Instant.now().isAfter(Instant.parse(fields.get("expiresAt")));
        } catch (Exception e) {
            return true;
        }
    }

    public LicenseResult validateLicensePayload(String json) {
        return validateLicenseJson(json);
    }

    private LicenseResult validateLicenseJson(String json) {
        try {
            Map<String, String> f = parseJsonFields(json);

            String licenseId = require(f, "licenseId");
            String userEmail = require(f, "userEmail");
            String plan = require(f, "plan");
            String issuedAt = require(f, "issuedAt");
            String expiresAt = require(f, "expiresAt");
            String nonce = require(f, "nonce");
            String payloadB64 = require(f, "payload");
            String payloadIv = require(f, "payloadIv");
            String payloadKey = require(f, "payloadKey");
            String signature = require(f, "signature");

            String canonical = String.join("|",
                    licenseId, userEmail, plan, issuedAt, expiresAt, nonce);

            if (!rsaVerify(canonical, signature, publicKey)) {
                return LicenseResult.fail("Signature verification failed — license may be tampered.");
            }

            byte[] aesKeyBytes = rsaUnwrapWithPublic(
                    Base64.getDecoder().decode(payloadKey), publicKey);

            byte[] iv = Base64.getDecoder().decode(payloadIv);
            byte[] encData = Base64.getDecoder().decode(payloadB64);
            byte[] decData = aesDecrypt(encData, aesKeyBytes, iv);
            String payload = new String(decData, StandardCharsets.UTF_8);

            Map<String, String> p = parseJsonFields(payload);

            if (!licenseId.equals(p.get("licenseId"))
                    || !userEmail.equals(p.get("userEmail"))
                    || !plan.equals(p.get("plan"))
                    || !issuedAt.equals(p.get("issuedAt"))
                    || !expiresAt.equals(p.get("expiresAt"))
                    || !nonce.equals(p.get("nonce"))) {
                return LicenseResult.fail("Payload fields do not match outer fields — license corrupted.");
            }

            Instant expiry = Instant.parse(expiresAt);
            if (Instant.now().isAfter(expiry)) {
                return LicenseResult.ok(plan, licenseId, userEmail, expiry);
            }

            return LicenseResult.ok(plan, licenseId, userEmail, expiry);

        } catch (LicenseFieldException e) {
            return LicenseResult.fail("Missing required field: " + e.getMessage());
        } catch (Exception e) {
            return LicenseResult.fail("Validation error: " + e.getMessage());
        }
    }

    private static boolean rsaVerify(String data, String signatureB64, PublicKey key) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(key);
        sig.update(data.getBytes(StandardCharsets.UTF_8));
        return sig.verify(Base64.getDecoder().decode(signatureB64));
    }

    private static byte[] rsaUnwrapWithPublic(byte[] packed, PublicKey publicKey) throws Exception {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packed));
        int sigLen = dis.readInt();
        byte[] signedKey = new byte[sigLen];
        dis.readFully(signedKey);

        byte[] encAesKey = dis.readAllBytes();

        byte[] derivedKey = Arrays.copyOf(sha256Bytes(signedKey), 32);
        byte[] iv = Arrays.copyOf(
                sha256Bytes(("IV:" + Base64.getEncoder().encodeToString(signedKey)).getBytes(StandardCharsets.UTF_8)), 16);

        return aesDecrypt(encAesKey, derivedKey, iv);
    }

    private static byte[] aesDecrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(data);
    }

    private static byte[] sha256Bytes(byte[] data) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(data);
    }

    private static PublicKey parsePublicKey(String pem) throws Exception {
        String stripped = pem
                .replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(stripped);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }

    private static Map<String, String> parseJsonFields(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        String body = json.trim();
        if (body.startsWith("{")) body = body.substring(1);
        if (body.endsWith("}"))  body = body.substring(0, body.length() - 1);

        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");
        java.util.regex.Matcher m = p.matcher(body);
        while (m.find()) {
            map.put(m.group(1), m.group(2));
        }
        return map;
    }

    private static String require(Map<String, String> map, String key) throws LicenseFieldException {
        String v = map.get(key);
        if (v == null || v.isEmpty()) throw new LicenseFieldException(key);
        return v;
    }

    private static class LicenseFieldException extends Exception {
        LicenseFieldException(String field) { super(field); }
    }

    private static String sanitizeUserId(String userId) {
        return userId.replaceAll("[^a-zA-Z0-9._\\-@]", "_");
    }
}