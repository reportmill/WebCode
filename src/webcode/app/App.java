/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package webcode.app;
import snap.web.*;

/**
 * WebCode App entry point.
 */
public class App {

/**
 * Main method to run panel.
 */
public static void main(final String args[])
{
    WebURL url = WebURL.getURL(App.class, "/testsite");
    WebSite site = url.getAsSite();
    AppPane apane = new AppPane();
    apane.addSite(site);
    apane.show();
}

}