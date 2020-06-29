// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/** Servlet that returns some example content. Can handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private ArrayList<String> commentHistory = new ArrayList<String>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //ArrayList<String> funFacts = createFacts();
    String json = convertToJson(commentHistory);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String name = request.getParameter("name");
    String comment = request.getParameter("comment");
    commentHistory.add(name + "said : \"" + comment "\"");

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /**
   * Create a list of the majors that SymSys consists of
   */
  private ArrayList<String> createFacts() {
    ArrayList<String> funFacts = new ArrayList<String>();
    funFacts.add("computer science");
    funFacts.add("linguistics");
    funFacts.add("psychology");
    funFacts.add("philosophy");
    return funFacts;
  }

  /**
   * Converts an ArrayList<String> into a JSON string.
   */
  private String convertToJson(ArrayList<String> comments) {
    String json = "{";
    json += "\"history\": [";
    for (int i = 0; i < comments.size(); i++) {
      String comment = comments.get(i);
      if (i != 0) {
        json += ", ";
      }
      json += "\"" + comment + "\"";
    }
    // json += comments;
    // json += ", \"ling\": ";
    // json += "\"" + facts.get(1) + "\"";
    // json += ", \"psych\": ";
    // json += "\"" + facts.get(2) + "\"";
    // json += ", \"phil\": ";
    // json += "\"" + facts.get(3) + "\"";
    json += "]}";
    return json;
  }
}
