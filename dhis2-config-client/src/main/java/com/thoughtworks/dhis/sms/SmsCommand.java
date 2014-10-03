package com.thoughtworks.dhis.sms;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.thoughtworks.dhis.Client;
import com.thoughtworks.dhis.endpoints.ApiService;
import com.thoughtworks.dhis.models.DataElement;
import com.thoughtworks.dhis.models.DataSet;
import com.thoughtworks.dhis.models.User;
import lombok.Getter;
import lombok.ToString;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.List;
import java.util.logging.Logger;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

@Getter
@ToString
public class SmsCommand {
    private static Logger logger = Logger.getLogger(SmsCommand.class.getName());

    private static final String DHIS_BASE_URL = "http://104.131.225.22:8888/dhis2";
    private static final String DHIS_LOGIN = "admin";
    private static final String DHIS_PASSWORD = "district";

    private static final List<String> ALLOWED_DATA_SETS = newArrayList(
            "ORDERED ROUTINE",
            "LMIS Emergency Order",
            "LMIS Commodities Default",
            "LMIS Commodities Allocated",
            "LMIS Commodities Losses"
    );

    private String name;
    private String dataSetName;
    private List<SmsDataEntry> dataEntries;

    public static List<SmsCommand> all() {
        ApiService apiService = getApiService();
        String requiredFields = "id,name,dataElements[name,id]";
        List<DataSet> allLmisDataSets = apiService.searchDataSets("LMIS", requiredFields).getDataSets();
        return from(allLmisDataSets).filter(new Predicate<DataSet>() {
            @Override
            public boolean apply(final DataSet dataSet) {
                return from(ALLOWED_DATA_SETS).filter(new Predicate<String>() {
                    @Override
                    public boolean apply(String allowedDataSet) {
                        return dataSet.getName().contains(allowedDataSet);
                    }
                }).toList().size() == 1;
            }
        }).transform(new Function<DataSet, SmsCommand>() {
            @Override
            public SmsCommand apply(DataSet dataSet) {
                return new SmsCommand(dataSet);
            }
        }).toList();
    }

    public static void publish(List<SmsCommand> smsCommands) {
        WebDriver driver = new FirefoxDriver();
        login(driver);

        for (SmsCommand smsCommand : smsCommands) {
            logger.info(format("Configuring SMS Command [%s] for %s", smsCommand.getName(), smsCommand.getDataSetName()));
            smsCommand.clear(driver);
            smsCommand.create(driver);
            smsCommand.configure(driver);
        }

        driver.quit();
    }

    private static ApiService getApiService() {
        return new Client(DHIS_BASE_URL + "/api/", new User(DHIS_LOGIN, DHIS_PASSWORD)).getService();
    }

    private SmsCommand(DataSet dataSet) {
        this.name = dataSet.getId();
        this.dataSetName = dataSet.getName();
        this.dataEntries = from(dataSet.getDataElements()).transform(new Function<DataElement, SmsDataEntry>() {
            @Override
            public SmsDataEntry apply(DataElement dataElement) {
                return new SmsDataEntry(dataElement);
            }
        }).toList();
    }

    private static void login(WebDriver driver) {
        // Login
        driver.get(DHIS_BASE_URL);
        driver.findElement(By.id("j_username")).sendKeys(DHIS_LOGIN);
        driver.findElement(By.id("j_password")).sendKeys(DHIS_PASSWORD);
        driver.findElement(By.id("submit")).click();
    }

    private void configure(WebDriver driver) {
        String xpathToEditLink = format("//tr[td[text()='%s ']]/td/a[text()='Edit']", getName());
        driver.findElement(By.xpath(xpathToEditLink)).click();

        driver.findElement(By.name("currentPeriodUsedForReporting")).click();
        driver.findElement(By.name("separator")).sendKeys(".");

        for (SmsDataEntry dataEntry : getDataEntries()) {
            String xpathToCodeInput =
                    format("//tr[td[contains(text(), '%s')]]/td/input[@type='text']", dataEntry.getName());
            driver.findElement(By.xpath(xpathToCodeInput)).sendKeys(dataEntry.getShortCode());
        }
        driver.findElement(By.xpath("//input[@type='button' and @value='Save']")).click();
    }

    private void create(WebDriver driver) {
        driver.get(DHIS_BASE_URL + "/dhis-web-maintenance-mobile/newSMSCommand.action");
        driver.findElement(By.id("name")).sendKeys(getName());
        driver.findElement(By.name("selectedDataSetID")).sendKeys(getDataSetName());
        driver.findElement(By.id("save")).click();
    }

    private void clear(WebDriver driver) {
        driver.get(DHIS_BASE_URL + "/dhis-web-maintenance-mobile/SMSCommands.action");
        String xpathToDeleteLink = format("//tr[td[text()='%s ']]/td/a[text()='Delete']", getName());
        try {
            while (true) {
                driver.findElement(By.xpath(xpathToDeleteLink)).click();
            }
        } catch (NoSuchElementException e) {
            // continue
        }
    }
}
