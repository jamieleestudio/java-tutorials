package com.third.li;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 启动时用 Java2D 生成一张示例图片（蓝色矩形 + 红色圆形 + 文字），
 * 这样模块无需外部图片资源即可演示视觉能力。
 */
@Component
public class SampleImageGenerator {

    private static final Logger log = LoggerFactory.getLogger(SampleImageGenerator.class);

    private final Path imageDir;

    public SampleImageGenerator(@Value("${demo.image-dir}") String imageDir) {
        this.imageDir = Path.of(imageDir);
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(imageDir);
        Path png = sampleImage();
        if (Files.notExists(png)) {
            BufferedImage image = new BufferedImage(480, 320, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            try {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, 480, 320);

                g.setColor(new Color(0x2F6FEB));
                g.fillRect(40, 40, 200, 120);

                g.setColor(new Color(0xE5484D));
                g.fillOval(280, 60, 140, 140);

                g.setColor(Color.BLACK);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
                g.drawString("EMBABEL", 40, 260);
            } finally {
                g.dispose();
            }
            ImageIO.write(image, "png", png.toFile());
        }
        log.info("Sample image ready at {}", png.toAbsolutePath());
    }

    public Path sampleImage() {
        return imageDir.resolve("sample.png");
    }
}
