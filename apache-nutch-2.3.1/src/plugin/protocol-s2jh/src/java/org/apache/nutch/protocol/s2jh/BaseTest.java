package org.apache.nutch.protocol.s2jh;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

/**
 * Base Test Class.
 * Nothing special: it's a simple way to write basic WebDriver tests and be able to switch
 * between different Drivers with minimum effort.
 */
public abstract class BaseTest {
    private WebDriver mDriver                      = null;

    private static final String CONFIG_FILE        = "config.ini";
    private static final String DRIVER_FIREFOX     = "firefox";
    private static final String DRIVER_CHROME      = "chrome";
    private static final String DRIVER_PHANTOMJS   = "phantomjs";

    private static long nonScientificTimer;

    protected static Properties sConfig;
    protected static DesiredCapabilities sCaps;

    private static boolean isUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException mue) {
            return false;
        }
    }

    @BeforeClass
    public static void configure() throws IOException {
        // Read config file
        sConfig = new Properties();
        sConfig.load(new FileReader(CONFIG_FILE));

        // Prepare capabilities
        sCaps = new DesiredCapabilities();
        sCaps.setJavascriptEnabled(true);
        sCaps.setCapability("takesScreenshot", false);

// 01
        // Change "User-Agent" via page-object capabilities
        sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", "My User Agent - Chrome");

// 02
        // Disable "web-security", enable all possible "ssl-protocols" and "ignore-ssl-errors" for PhantomJSDriver
        sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[] {
            "--web-security=false",
            "--ssl-protocol=any",
            "--ignore-ssl-errors=true",
            "--webdriver-loglevel=DEBUG"
        });

// 03 (UPCOMING)
//        // Control LogLevel for GhostDriver, via CLI arguments
//        sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_CLI_ARGS, new String[] {
//            "--logLevel=" + (sConfig.getProperty("phantomjs_driver_loglevel") != null ? sConfig.getProperty("phantomjs_driver_loglevel") : "INFO")
//        });

// OPTIONAL
//        // Fetch configuration parameters
//        // "phantomjs_exec_path"
//        if (sConfig.getProperty("phantomjs_exec_path") != null) {
//            sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, sConfig.getProperty("phantomjs_exec_path"));
//        } else {
//            throw new IOException(String.format("Property '%s' not set!", PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY));
//        }
//        // "phantomjs_driver_path"
//        if (sConfig.getProperty("phantomjs_driver_path") != null) {
//            System.out.println("Test will use an external GhostDriver");
//            sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_PATH_PROPERTY, sConfig.getProperty("phantomjs_driver_path"));
//        } else {
//            System.out.println("Test will use PhantomJS internal GhostDriver");
//        }
    }

    @Before
    public void testAboutToStart() {
        resetTimer();
    }

    @Before
    public void prepareDriver() throws Exception
    {
        // Which driver to use? (default "phantomjs")
        String driver = sConfig.getProperty("driver", DRIVER_PHANTOMJS);
        System.out.println("* SELECTED DRIVER: " + driver);

        // Start appropriate Driver
        if (isUrl(driver)) {
            mDriver = new RemoteWebDriver(new URL(driver), sCaps);
        } else if (driver.equals(DRIVER_FIREFOX)) {
            mDriver = new FirefoxDriver(sCaps);
        } else if (driver.equals(DRIVER_CHROME)) {
            mDriver = new ChromeDriver(sCaps);
        } else if (driver.equals(DRIVER_PHANTOMJS)) {
            mDriver = new PhantomJSDriver(sCaps);
        }
    }

    protected WebDriver getDriver() {
        return mDriver;
    }

    @After
    public void quitDriver() {
        if (mDriver != null) {
            mDriver.quit();
            mDriver = null;
        }
    }

    @After
    public void testFinished() {
        System.out.println("* TEST FINISHED IN (ms): " + elapsedSeconds());
    }

    public void resetTimer() {
        nonScientificTimer = new Date().getTime();
    }

    public long elapsedSeconds() {
        return ((new Date().getTime()) - nonScientificTimer);
    }
}
