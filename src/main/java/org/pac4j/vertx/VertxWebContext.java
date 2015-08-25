/*
  Copyright 2014 - 2014 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.vertx;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.pac4j.core.context.WebContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * WebContext implementation for Vert.x 3.
 *
 * @author Jeremy Prime
 * @since 1.0.0
 *
 */
public class VertxWebContext implements WebContext {

  private final Pac4jSessionAttributes sessionAttributes;
  private final RoutingContext routingContext;
  private final String method;
  private final String serverName;
  private final int serverPort;
  private final String fullUrl;
  private final String scheme;
  private final String remoteAddress;
  private final JsonObject headers;
  private final JsonObject parameters;
  private final Map<String, String[]> mapParameters;

  private final JsonObject outHeaders = new JsonObject();
  private final StringBuilder sb = new StringBuilder();
  private int code;

  public VertxWebContext(final RoutingContext routingContext, final Pac4jSessionAttributes sessionAttributes) {
    final HttpServerRequest request = routingContext.request();
    this.routingContext = routingContext;
    this.method = request.method().toString();

    this.fullUrl = request.absoluteURI();
    URI uri;
    try {
      uri = new URI(fullUrl);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      throw new InvalidParameterException("Request to invalid URL " + fullUrl + " while constructing VertxWebContext");
    }
    this.scheme = uri.getScheme();
    this.serverName = uri.getHost();
    this.serverPort = (uri.getPort() != -1) ? uri.getPort() : scheme.equals("http") ? 80 : 443;
    this.remoteAddress = request.remoteAddress().toString();

    headers = new JsonObject();
    for (String name : request.headers().names()) {
      headers.put(name, request.headers().get(name));
    }

    parameters = new JsonObject();
    for (String name : request.params().names()) {
      parameters.put(name, new JsonArray(Arrays.asList(request.params().getAll(name).toArray())));
    }

    mapParameters = new HashMap<>();
    for (String name : parameters.fieldNames()) {
      JsonArray params = parameters.getJsonArray(name);
      String[] values = new String[params.size()];
      int i = 0;
      for (Object o : params) {
        values[i++] = (String) o;
      }
      mapParameters.put(name, values);
    }

    this.sessionAttributes = sessionAttributes;
  }

  @Override
  public String getRequestParameter(String name) {
    JsonArray values = parameters.getJsonArray(name);
    if (values != null && values.size() > 0) {
      return values.getString(0);
    }
    return null;
  }

  @Override
  public Map<String, String[]> getRequestParameters() {
    return mapParameters;
  }

  @Override
  public Object getRequestAttribute(String s) {
    return routingContext.get(s);
  }

  @Override
  public void setRequestAttribute(String s, Object o) {
    routingContext.put(s, o);
  }

  @Override
  public String getRequestHeader(String name) {
    return headers.getString(name);
  }

  @Override
  public void setSessionAttribute(String name, Object value) {
    sessionAttributes.getCustomAttributes().put(name, value);
  }

  @Override
  public Object getSessionAttribute(String name) {
    return sessionAttributes.getCustomAttributes().get(name);
  }

  @Override
  public void invalidateSession() {
      this.sessionAttributes.setUserProfile(null);
      this.sessionAttributes.getCustomAttributes().clear();
  }

  public Pac4jSessionAttributes getSessionAttributes() {
    return sessionAttributes;
  }

  @Override
  public String getRequestMethod() {
    return method;
  }

  @Override
  public String getRemoteAddr() {
    return remoteAddress;
  }

  @Override
  public void writeResponseContent(String content) {
    sb.append(content);
  }

  public String getResponseContent() {
    return sb.toString();
  }

  @Override
  public void setResponseStatus(int code) {
    this.code = code;
  }

  public int getResponseStatus() {
    return code;
  }

  @Override
  public void setResponseHeader(String name, String value) {
    outHeaders.put(name, value);
  }

  public JsonObject getResponseHeaders() {
    return outHeaders;
  }

  @Override
  public String getServerName() {
    return serverName;
  }

  @Override
  public int getServerPort() {
    return serverPort;
  }

  @Override
  public String getScheme() {
    return scheme;
  }

  @Override
  public String getFullRequestURL() {
    return fullUrl;
  }

}