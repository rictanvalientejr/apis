/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.surfnet.oaaas.consent;

import java.io.IOException;

import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpMethod;
import org.surfnet.oaaas.auth.AbstractUserConsentHandler;
import org.surfnet.oaaas.model.Client;

/**
 * Example {@link AbstractUserConsentHandler} that forwards to a form.
 * 
 */
@Named("formConsentHandler")
public class FormUserConsentHandler extends AbstractUserConsentHandler {

  private static final String USER_OAUTH_APPROVAL = "user_oauth_approval";

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.surfnet.oaaas.auth.AbstractUserConsentHandler#handleUserConsent(javax
   * .servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
   * javax.servlet.FilterChain, java.lang.String, java.lang.String,
   * org.surfnet.oaaas.model.Client)
   */
  @Override
  public void handleUserConsent(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
      String authStateValue, String returnUri, Client client) throws IOException, ServletException {
    if (isUserConsentPost(request)) {
      processForm(request);
      chain.doFilter(request, response);
    } else {
      processInitial(request, response, returnUri, authStateValue, client);
    }
  }

  private boolean isUserConsentPost(HttpServletRequest request) {
    String oauthApproval = request.getParameter(USER_OAUTH_APPROVAL);
    return request.getMethod().equals(HttpMethod.POST.toString()) && StringUtils.isNotBlank(oauthApproval);
  }

  private void processInitial(HttpServletRequest request, ServletResponse response, String returnUri,
      String authStateValue, Client client) throws IOException, ServletException {
    request.setAttribute("client", client);
    request.setAttribute(AUTH_STATE, authStateValue);
    request.setAttribute("actionUri", returnUri);
    request.getRequestDispatcher(getUserConsentUrl()).forward(request, response);
  }

  /**
   * 
   * Return the path to the User Consent page. Subclasses can use this hook by
   * providing a custom html/jsp.
   * 
   * @return the path to the User Consent page
   */
  protected String getUserConsentUrl() {
    return "/WEB-INF/jsp/userconsent.jsp";
  }

  private void processForm(final HttpServletRequest request) {
    if (Boolean.valueOf(request.getParameter(USER_OAUTH_APPROVAL))) {
      setAuthStateValue(request, request.getParameter(AUTH_STATE));
      String[] scopes = request.getParameterValues(GRANTED_SCOPES);
      setScopes(request, scopes);
    }
  }

}
