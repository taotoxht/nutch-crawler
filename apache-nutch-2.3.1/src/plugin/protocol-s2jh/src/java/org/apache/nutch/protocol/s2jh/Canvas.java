package org.apache.nutch.protocol.s2jh;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;

import java.io.File;
import java.io.IOException;

public class Canvas {
    private WebDriver driver;
    private JavascriptExecutor jsExecutor;

    public Canvas(WebDriver d) {
        driver = d;
        jsExecutor = (JavascriptExecutor)d;

        // Prepare page
        jsExecutor.executeScript("document.write(\"" +
                "<html><body><canvas id='surface'></canvas></body></html>\");");
        jsExecutor.executeScript("document.body.style.margin = '0px';");
    }

    public void setCanvasSize(int width, int height) {
        driver.manage().window().setSize(new Dimension(width, height));
    }

    public void setBackgroundColor(String cssValidColor) {
        jsExecutor.executeScript("document.body.style.backgroundColor = '"+ cssValidColor +"';");
    }

    public void drawColorWheel() {
        jsExecutor.executeScript("var el = document.getElementById('surface'),\n" +
                "        context = el.getContext('2d'),\n" +
                "        width = window.innerWidth,\n" +
                "        height = window.innerHeight,\n" +
                "        cx = width / 2,\n" +
                "        cy = height / 2,\n" +
                "        radius = width  / 2.3,\n" +
                "        imageData,\n" +
                "        pixels,\n" +
                "        hue, sat, value,\n" +
                "        i = 0, x, y, rx, ry, d,\n" +
                "        f, g, p, u, v, w, rgb;\n" +
                "\n" +
                "    el.width = width;\n" +
                "    el.height = height;\n" +
                "    imageData = context.createImageData(width, height);\n" +
                "    pixels = imageData.data;\n" +
                "\n" +
                "    for (y = 0; y < height; y = y + 1) {\n" +
                "        for (x = 0; x < width; x = x + 1, i = i + 4) {\n" +
                "            rx = x - cx;\n" +
                "            ry = y - cy;\n" +
                "            d = rx * rx + ry * ry;\n" +
                "            if (d < radius * radius) {\n" +
                "                hue = 6 * (Math.atan2(ry, rx) + Math.PI) / (2 * Math.PI);\n" +
                "                sat = Math.sqrt(d) / radius;\n" +
                "                g = Math.floor(hue);\n" +
                "                f = hue - g;\n" +
                "                u = 255 * (1 - sat);\n" +
                "                v = 255 * (1 - sat * f);\n" +
                "                w = 255 * (1 - sat * (1 - f));\n" +
                "                pixels[i] = [255, v, u, u, w, 255, 255][g];\n" +
                "                pixels[i + 1] = [w, 255, 255, v, u, u, w][g];\n" +
                "                pixels[i + 2] = [u, u, w, 255, 255, v, u][g];\n" +
                "                pixels[i + 3] = 255;\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "context.putImageData(imageData, 0, 0);");
    }

    public void saveCanvasContent(String filePath) throws IOException {
        File memFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(memFile, new File(filePath));
    }
}
