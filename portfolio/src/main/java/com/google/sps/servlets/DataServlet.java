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
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/** Servlet that handles comments data -- adding and displaying comments */
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
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    ArrayList<ArrayList<String>> commentHistory = new ArrayList<ArrayList<String>>();
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty("name");
      String text = (String) entity.getProperty("comment");
      String emoji = (String) entity.getProperty("emoji");
      String email = (String) entity.getProperty("email");
      long timestamp = (long) entity.getProperty("timestamp");
      String imageUrl = (String) entity.getProperty("image");

      String commentText = name + " (" + email + ") said: " + text;
      if (!emoji.equals("None")) commentText += " " + emoji;
      //if (!imageUrl.equals(null)) commentText += "<img src=\"" + imageUrl + "\" />";
      ArrayList<String> commentWithImage = new ArrayList<String>();
      commentWithImage.add(commentText);
      commentWithImage.add(imageUrl);
      commentHistory.add(commentWithImage);

      numComments -= 1;
      if (numComments == 0) break;
    }
    String commentJson = convertToJson(commentHistory);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().println(commentJson);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String name = request.getParameter("name");
    String comment = request.getParameter("comment");
    String emoji = request.getParameter("emoji");
    String email = "";
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      email = userService.getCurrentUser().getEmail();
    }
    // Get the URL of the image that the user uploaded to Blobstore.
    String imageUrl = getUploadedFileUrl(request, "image");

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("comment", comment);
    commentEntity.setProperty("emoji", emoji);
    commentEntity.setProperty("email", email);
    commentEntity.setProperty("timestamp", System.currentTimeMillis());
    commentEntity.setProperty("image", imageUrl);

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

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) return null;

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }

  /**
   * Converts a list of comments into a JSON string.
   */
  private String convertToJson(ArrayList<ArrayList<String>> comments) {
    String json = "{";
    json += "\"history\": [";
    for (int i = 0; i < comments.size(); i++) {
      ArrayList<String> commentWithImage = comments.get(i);
      if (i != 0) json += ", ";
      json += "[\"" + commentWithImage.get(0) + "\", \"";
      String imageUrl = commentWithImage.get(1);
      if (imageUrl == null) json += "";
      else json += imageUrl;
      json += "\"]";
    }
    json += "]}";
    return json;
  }
}