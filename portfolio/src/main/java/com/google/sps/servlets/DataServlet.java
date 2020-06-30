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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/** Servlet that returns some example content. Can handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  // Shows all comments by default
  private int maxCommentNumber = -1;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int numComments = getMaxNumComments(request);
    if (numComments == -1) {
      response.setContentType("text/html");
      response.getWriter().println("Please enter a nonnegative integer.");
      return;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("Comment");
    PreparedQuery results = datastore.prepare(query);
    ArrayList<String> commentHistory = new ArrayList<String>();
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty("name");
      String text = (String) entity.getProperty("comment");

      String wholeComment = name + " said: " + text;
      commentHistory.add(wholeComment);

      numComments -= 1;
      if (numComments == 0) break;
    }
    String commentJson = convertToJson(commentHistory);
    response.setContentType("application/json;");
    response.getWriter().println(commentJson);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String name = request.getParameter("name");
    String comment = request.getParameter("comment");
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("comment", comment);

    // Store comment permanently
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /** Returns the maximum number of comments entered by the user, or -1 if the choice was invalid. */
  private int getMaxNumComments(HttpServletRequest request) {
    // Get the input from the form.
    String maxCommentsString = request.getParameter("max-comments");

    // Convert the input to an int.
    int maxComments;
    try {
      maxComments = Integer.parseInt(maxCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + maxCommentsString);
      return -1;
    }

    // Check that the input is greater than 0.
    if (maxComments < 1) {
      System.err.println("Player choice is out of range: " + maxCommentsString);
      return -1;
    }

    return maxComments;
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
    json += "]}";
    return json;
  }
}
