// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import java.net.MalformedURLException;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;

public class ImportAndViewResponder implements SecureResponder, WikiImporterClient {
  private WikiPage page;

  public Response makeResponse(FitNesseContext context, Request request) throws MalformedURLException {
    String resource = request.getResource();

    if ("".equals(resource))
      resource = "FrontPage";

    loadPage(resource, context);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);
    loadPageData();

    SimpleResponse response = new SimpleResponse();
    response.redirect(resource);

    return response;
  }

  protected void loadPage(String resource, FitNesseContext context) {
    WikiPagePath path = PathParser.parse(resource);
    PageCrawler crawler = context.root.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    page = crawler.getPage(context.root, path);
  }

  protected void loadPageData() throws MalformedURLException {
    PageData pageData = page.getData();

    WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());

    if (importProperty != null) {
      WikiImporter importer = new WikiImporter();
      importer.setWikiImporterClient(this);
      importer.parseUrl(importProperty.getSourceUrl());
      importer.importRemotePageContent(page);
    }
  }

  public void pageImported(WikiPage localPage) {
  }

  public void pageImportError(WikiPage localPage, Exception e) {
    e.printStackTrace();
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
