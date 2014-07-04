package com.github.timmpermeance;

import static org.junit.Assert.assertTrue;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

public class AwtRobotTest {

    private WebDriver driver;
    private Robot robot;

    private static final int MANUAL = 1;

    @Before
    public void setup() throws Exception {
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        // Select the device with 0,0 co-ordinates to match webDriver
        for (GraphicsDevice graphicsDevice : devices) {

            if (graphicsDevice.getDefaultConfiguration().getBounds().contains(0, 0)) {
                robot = new Robot(graphicsDevice);
                break;
            }
        }

        FirefoxProfile profile = new FirefoxProfile();

        // Configure proxy if required
        if (Boolean.parseBoolean(System.getProperty("useProxy", "false"))) {

            String proxyHost = System.getProperty("http.proxy.host", "");
            int proxyPort = Integer.parseInt(System.getProperty("http.proxy.port", "0"));

            profile.setPreference("network.proxy.type", MANUAL);
            profile.setPreference("network.proxy.http", proxyHost);
            profile.setPreference("network.proxy.http_port", proxyPort);
            profile.setPreference("network.proxy.ssl", proxyHost);
            profile.setPreference("network.proxy.ssl_port", proxyPort);

        }

        driver = new FirefoxDriver(profile);

        driver.manage().window().setPosition(new Point(0, 0));
        driver.manage().window().maximize();

    }

    @After
    public void close() throws Exception {
        driver.quit();
    }

    @Test
    public void performGoogleSearch() throws Exception {

        driver.get("http://www.google.com.au");

        WebElement queryBox = driver.findElement(By.id("gbqfq"));

        // Based on
        // http://ardesco.lazerycode.com/index.php/2012/12/hacking-mouse-move-events-into-safari-driver-the-nasty-way/

        // // Get Browser dimensions
        int browserWidth = driver.manage().window().getSize().width;
        int browserHeight = driver.manage().window().getSize().height;
        //
        // // Get dimensions of the window displaying the web page
        int pageWidth = Integer.parseInt(executeJavascript("return document.documentElement.clientWidth").toString());
        int pageHeight = Integer.parseInt(executeJavascript("return document.documentElement.clientHeight").toString());
        //
        // // Calculate the space the browser is using for toolbars
        int browserFurnitureOffsetX = browserWidth - pageWidth;
        int browserFurnitureOffsetY = browserHeight - pageHeight;

        int elemTopLeftX = queryBox.getLocation().getX();
        int elemTopLeftY = queryBox.getLocation().getY();

        int elemHeight = queryBox.getSize().height;
        int elemWidth = queryBox.getSize().width;

        int elemCentreX = elemTopLeftX + Math.round(elemWidth / 2);
        int elemCentreY = elemTopLeftY + Math.round(elemHeight / 2);

        int elemXonScreen = driver.manage().window().getPosition().x + browserFurnitureOffsetX + elemCentreX;
        int elemYonScreen = driver.manage().window().getPosition().y + browserFurnitureOffsetY + elemCentreY;

        robot.mouseMove(elemXonScreen, elemYonScreen);
        robot.waitForIdle();
        robot.mousePress(InputEvent.getMaskForButton(MouseEvent.BUTTON1));
        robot.mouseRelease(InputEvent.getMaskForButton(MouseEvent.BUTTON1));
        typeString("Robots");

        typeKey(KeyEvent.VK_ENTER, false);

        FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver).ignoring(NoSuchElementException.class,
                StaleElementReferenceException.class).withTimeout(30, TimeUnit.SECONDS);
        By by = By.linkText("Robot - Wikipedia, the free encyclopedia");
        wait.until(ExpectedConditions.presenceOfElementLocated(by));

        WebElement element = driver.findElement(by);

        // now lets look at robots on Wikipedia
        element.click();

        WebElement pTag = driver.findElement(By.tagName("p"));
        assertTrue(pTag
                .getText()
                .contains(
                        "A robot is a mechanical or virtual artificial agent, usually an electro-mechanical machine that is guided by a computer program or electronic circuitry."));

    }

    public void typeString(final String letters) {
        for (char letter : letters.toCharArray()) {
            typeCharacter("" + letter);
        }
    }

    public void typeCharacter(final String letter) {
        // Adapted from
        // http://stackoverflow.com/questions/8875092/robot-class-java-typing-a-string-issue
        try {
            boolean upperCase = Character.isUpperCase(letter.charAt(0));
            String variableName = "VK_" + letter.toUpperCase();

            Class clazz = KeyEvent.class;
            Field field = clazz.getField(variableName);
            int keyCode = field.getInt(null);

            typeKey(keyCode, upperCase);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void typeKey(final int keyCode, final boolean upperCase) {
        robot.delay(1000);

        if (upperCase) {
            robot.keyPress(KeyEvent.VK_SHIFT);
        }

        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);

        if (upperCase) {
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }
    }

    private Object executeJavascript(final String script) {
        return ((JavascriptExecutor) driver).executeScript(script);
    }

}
