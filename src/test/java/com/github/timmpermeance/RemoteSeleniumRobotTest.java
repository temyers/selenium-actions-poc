package com.github.timmpermeance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.seleniumhq.jetty7.server.Handler;
import org.seleniumhq.jetty7.server.Server;
import org.seleniumhq.jetty7.server.handler.DefaultHandler;
import org.seleniumhq.jetty7.server.handler.HandlerList;
import org.seleniumhq.jetty7.server.handler.ResourceHandler;

import com.github.timmpermeance.test.RepeatRule;

public class RemoteSeleniumRobotTest {

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    private WebDriver driver;

    private static Server server;

    @BeforeClass
    public static void startWebServer() throws Exception {

        server = new Server(8080);
        // Configure the ResourceHandler to serve test resource files
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setResourceBase("target/test-classes/html");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {resource_handler, new DefaultHandler()});
        server.setHandler(handlers);

        server.start();
    }

    @AfterClass
    public static void shutdownWebServer() throws Exception {
        server.stop();
    }

    @Before
    public void setup() throws Exception {
        // Chrome uses a remote driver executable which makes it good for intercepting commands for
        // prototyping.
        DesiredCapabilities capabilities = new DesiredCapabilities("robotChrome", "", Platform.MAC);
        URL gridHub = new URL("http://10.25.67.130:4444/wd/hub/");
        driver = new RemoteWebDriver(gridHub, capabilities);

        // driver = new FirefoxDriver();

        driver.manage().window().setPosition(new Point(0, 0));
        driver.manage().window().maximize();

    }

    @After
    public void closeBrowser() throws Exception {
        driver.quit();
    }

    @Test
    public void hoverOverText() {
        driver.navigate().to("http://localhost:8080/page-with-hover.html");

        WebElement hoverElement = driver.findElement(By.id("hover"));
        WebElement shownOnHover = driver.findElement(By.id("shownOnHover"));

        assertFalse(shownOnHover.isDisplayed());

        Actions actions = new Actions(driver);
        actions.moveToElement(hoverElement);
        actions.perform();

        assertTrue(shownOnHover.isDisplayed());
    }

}
