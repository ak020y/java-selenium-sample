package com.lambdatest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.openqa.selenium.By;
import org.openqa.selenium.HasAuthentication;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class BasicAuthentication {
    public static String hubURL = "https://hub.lambdatest.com/wd/hub";
    private WebDriver driver;

    public void setup() throws MalformedURLException {
        ChromeOptions browserOptions = new ChromeOptions();
browserOptions.setPlatformName("Windows 10");
browserOptions.setBrowserVersion("122.0");
HashMap<String, Object> ltOptions = new HashMap<String, Object>();
ltOptions.put("username", "akumar35");
ltOptions.put("accessKey", "lO9FTrLzpca2FYrjcSaRwUt5ouzrXyPU8sSM7V7DRxP4Mm9aqP");
ltOptions.put("visual", true);
ltOptions.put("project", "Untitled");
ltOptions.put("name", "FirstTest");
ltOptions.put("w3c", true);
ltOptions.put("plugin", "java-java");
browserOptions.setCapability("LT:Options", ltOptions);
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", "Chrome");
        capabilities.setCapability("browserVersion", "95");
        HashMap<String, Object> ltOptions = new HashMap<String, Object>();
        ltOptions.put("user", System.getenv("LT_USERNAME"));
        ltOptions.put("accessKey", System.getenv("LT_ACCESS_KEY"));
        ltOptions.put("build", "Selenium 4");
        ltOptions.put("name", this.getClass().getName());
        ltOptions.put("platformName", "Windows 10");
        ltOptions.put("seCdp", true);
        ltOptions.put("selenium_version", "4.0.0");
        capabilities.setCapability("LT:Options", ltOptions);

        driver = new RemoteWebDriver(new URL(hubURL), capabilities);
        System.out.println(driver);
    }

    public void authentication() {
        Augmenter augmenter = new Augmenter();
        driver = augmenter.augment(driver);

        DevTools devTools = ((HasDevTools) driver).getDevTools();
        devTools.createSession();

        driver = augmenter.addDriverAugmentation("chrome", HasAuthentication.class,
                (caps, exec) -> (whenThisMatches, useTheseCredentials) -> devTools.getDomains().network()
                        .addAuthHandler(whenThisMatches, useTheseCredentials))
                .augment(driver);

        ((HasAuthentication) driver).register(UsernameAndPassword.of("foo", "bar"));

        driver.get("http://httpbin.org/basic-auth/foo/bar");

        String text = driver.findElement(By.tagName("body")).getText();
        System.out.println(text);
        if (text.contains("authenticated")) {
            markStatus("passed", "Authentication Successful", driver);
        } else {
            markStatus("failed", "Authentication Failure", driver);
        }

    }

    public void tearDown() {
        try {
            driver.quit();
        } catch (

        Exception e) {
            markStatus("failed", "Got exception!", driver);
            e.printStackTrace();
            driver.quit();
        }
    }

    public static void markStatus(String status, String reason, WebDriver driver) {
        JavascriptExecutor jsExecute = (JavascriptExecutor) driver;
        jsExecute.executeScript("lambda-status=" + status);
        System.out.println(reason);
    }

    public static void main(String[] args) throws MalformedURLException, InterruptedException {
        BasicAuthentication test = new BasicAuthentication();
        test.setup();
        test.authentication();
        test.tearDown();
    }
}
