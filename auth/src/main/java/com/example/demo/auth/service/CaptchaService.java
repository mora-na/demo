package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthConstants;
import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.dto.CaptchaResponse;
import com.example.demo.auth.store.CaptchaStore;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 验证码服务，生成图片验证码并进行验证。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int BASE_FONT_STYLE = Font.BOLD;

    private final AuthProperties authProperties;
    private final CaptchaStore captchaStore;
    private final AuthConstants systemConstants;
    private final AtomicBoolean fontsInitialized = new AtomicBoolean(false);
    private volatile List<Font> embeddedFonts = Collections.emptyList();

    private void ensureEmbeddedFontsInitialized() {
        if (fontsInitialized.get()) {
            return;
        }
        synchronized (this) {
            if (fontsInitialized.get()) {
                return;
            }
            initEmbeddedFonts();
            fontsInitialized.set(true);
        }
    }

    private void initEmbeddedFonts() {
        List<String> resources = authProperties.getCaptcha().getFontResources();
        if (resources == null || resources.isEmpty()) {
            return;
        }
        List<Font> loaded = new ArrayList<>();
        for (String resource : resources) {
            if (resource == null || resource.trim().isEmpty()) {
                continue;
            }
            try (InputStream inputStream = openFontResource(resource)) {
                if (inputStream == null) {
                    continue;
                }
                Font base = Font.createFont(Font.TRUETYPE_FONT, inputStream);
                loaded.add(base);
                try {
                    GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
                } catch (Exception ignore) {
                }
            } catch (Exception ignore) {
            }
        }
        if (!loaded.isEmpty()) {
            embeddedFonts = Collections.unmodifiableList(loaded);
        }
    }

    /**
     * 生成验证码并缓存验证码值。
     *
     * @return 验证码响应信息
     */
    public CaptchaResponse createCaptcha(String scopeKey) {
        AuthProperties.Captcha config = authProperties.getCaptcha();
        if (!captchaStore.allowCreate(scopeKey, config.getMaxEntries(), config.getCleanupIntervalSeconds())) {
            return null;
        }
        ensureEmbeddedFontsInitialized();
        Captcha captcha = createCaptchaImage(
                config.getWidth(),
                config.getHeight(),
                config.getCodeLength(),
                config.getThickness()
        );
        String captchaId = UUID.randomUUID().toString();
        long expireAt = Instant.now().getEpochSecond() + config.getExpireSeconds();
        captchaStore.save(captchaId, captcha.code, expireAt);
        String imageBase64 = systemConstants.getCaptcha().getImagePrefix() + captcha.base64;
        return new CaptchaResponse(captchaId, imageBase64, config.getExpireSeconds());
    }

    /**
     * 校验验证码并在校验后删除缓存。
     *
     * @param captchaId   验证码 ID
     * @param captchaCode 用户输入的验证码
     * @return 校验通过返回 true
     */
    public boolean verify(String captchaId, String captchaCode) {
        return captchaStore.verifyAndRemove(captchaId, captchaCode);
    }

    private Captcha createCaptchaImage(int width, int height, int codeLength, int noiseLines) {
        String code = randomCode(codeLength);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            drawNoiseLines(g, width, height, Math.max(0, noiseLines));
            int minDots = systemConstants.getCaptcha().getNoiseDotMinCount();
            int densityDivisor = Math.max(1, systemConstants.getCaptcha().getNoiseDotDensityDivisor());
            drawNoiseDots(g, width, height, Math.max(minDots, (width * height) / densityDivisor));
            drawChars(g, code, width, height);
        } finally {
            g.dispose();
        }
        return new Captcha(code, toBase64Png(image));
    }

    private void drawNoiseLines(Graphics2D g, int width, int height, int count) {
        for (int i = 0; i < count; i++) {
            g.setColor(randomColor(
                    systemConstants.getCaptcha().getLineDotColorMin(),
                    systemConstants.getCaptcha().getLineDotColorMax()
            ));
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawChars(Graphics2D g, String code, int width, int height) {
        int fontSize = Math.max(
                systemConstants.getCaptcha().getFontMinSize(),
                height - systemConstants.getCaptcha().getFontPadding()
        );
        Font baseFont = resolveFont(fontSize);
        g.setFont(baseFont);
        FontMetrics metrics = g.getFontMetrics(baseFont);
        int charCount = code.length();
        int gap = width / (charCount + 1);
        int baseY = (height - metrics.getHeight()) / 2 + metrics.getAscent();
        AffineTransform original = g.getTransform();
        for (int i = 0; i < charCount; i++) {
            char ch = code.charAt(i);
            Font font = resolveFont(fontSize);
            g.setFont(font);
            g.setColor(randomColor(
                    systemConstants.getCaptcha().getCharColorMin(),
                    systemConstants.getCaptcha().getCharColorMax()
            ));
            int x = gap * (i + 1) - metrics.charWidth(ch) / 2;
            double centerX = x + metrics.charWidth(ch) / 2.0;
            double centerY = baseY - metrics.getAscent() / 2.0;
            AuthProperties.Captcha config = authProperties.getCaptcha();
            double angle = randomRange(config.getRotateMin(), config.getRotateMax());
            double shearX = randomRange(config.getShearXMin(), config.getShearXMax());
            double shearY = randomRange(config.getShearYMin(), config.getShearYMax());
            AffineTransform transform = new AffineTransform(original);
            transform.translate(centerX, centerY);
            transform.rotate(angle);
            transform.shear(shearX, shearY);
            transform.translate(-centerX, -centerY);
            g.setTransform(transform);
            g.drawString(String.valueOf(ch), x, baseY);
        }
        g.setTransform(original);
    }

    private String randomCode(int length) {
        if (length <= 0) {
            return "";
        }
        String charset = systemConstants.getCaptcha().getCodeCharset();
        char[] captchaChars = charset == null || charset.isEmpty()
                ? AuthConstants.Captcha.DEFAULT_CODE_CHARSET.toCharArray()
                : charset.toCharArray();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(captchaChars[RANDOM.nextInt(captchaChars.length)]);
        }
        return sb.toString();
    }

    private Color randomColor(int min, int max) {
        int range = Math.max(0, max - min);
        int r = min + RANDOM.nextInt(range + 1);
        int g = min + RANDOM.nextInt(range + 1);
        int b = min + RANDOM.nextInt(range + 1);
        return new Color(r, g, b);
    }

    private void drawNoiseDots(Graphics2D g, int width, int height, int count) {
        for (int i = 0; i < count; i++) {
            g.setColor(randomColor(
                    systemConstants.getCaptcha().getLineDotColorMin(),
                    systemConstants.getCaptcha().getLineDotColorMax()
            ));
            int x = RANDOM.nextInt(width);
            int y = RANDOM.nextInt(height);
            int size = 1 + RANDOM.nextInt(2);
            g.fillRect(x, y, size, size);
        }
    }

    private String toBase64Png(BufferedImage image) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, systemConstants.getCaptcha().getPngFormat(), out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render captcha", e);
        }
    }

    private Font resolveFont(int fontSize) {
        Font base = pickEmbeddedFont();
        if (base != null) {
            return base.deriveFont(Font.BOLD, (float) fontSize);
        }
        return new Font(systemConstants.getCaptcha().getFallbackFontFamily(), Font.BOLD, fontSize);
    }

    private Font pickEmbeddedFont() {
        List<Font> fonts = embeddedFonts;
        if (fonts == null || fonts.isEmpty()) {
            return null;
        }
        return fonts.get(RANDOM.nextInt(fonts.size()));
    }

    private double randomRange(double min, double max) {
        double low = min;
        double high = max;
        if (high < low) {
            double tmp = low;
            low = high;
            high = tmp;
        }
        return low + (high - low) * RANDOM.nextDouble();
    }

    private InputStream openFontResource(String resource) throws Exception {
        String path = resource.trim();
        String classpathPrefix = systemConstants.getCaptcha().getFontResourceClasspathPrefix();
        if (classpathPrefix == null || classpathPrefix.isEmpty()) {
            classpathPrefix = AuthConstants.Captcha.DEFAULT_FONT_RESOURCE_CLASSPATH_PREFIX;
        }
        if (path.startsWith(classpathPrefix)) {
            path = path.substring(classpathPrefix.length());
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        ClassPathResource classPathResource = new ClassPathResource(path);
        if (!classPathResource.exists()) {
            return null;
        }
        return classPathResource.getInputStream();
    }

    private static class Captcha {
        private final String code;
        private final String base64;

        private Captcha(String code, String base64) {
            this.code = code;
            this.base64 = base64;
        }
    }
}
