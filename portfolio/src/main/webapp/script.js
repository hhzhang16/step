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

/**
 * Adds a random quote to the page.
 */
function addQuote() {
  const quotes = [
    '"It\'s fine to celebrate success but it is more important to heed the lessons of failure." - Bill Gates',
    '"There are no secrets to success. It is the result of preparation, hard work, and learning from failure." - Colin Powell',
    '"One child, one teacher, one book, one pen can change the world." - Malala Yousafzai',
    '"Live as if you were to die tomorrow. Learn as if you were to live forever." - Mahatma Gandhi',
    '"Do right. Do your best. Treat others as you want to be treated." - Lou Holtz',
    '"Just do the best you can. No one can do more than that." - John Wooden',
    '"I\'ve learned you are never too small to make a difference." - Greta Thunberg',
    '"Start where you are, use what you have, do what you can." - Arthur Ashe'
  ];
  console.log("Creating a greeting");

  // Pick a random quote.
  const quote = quotes[Math.floor(Math.random() * quotes.length)];
  console.log(quote);

  // Add it to the page.
  const quoteContainer = document.getElementById('greeting-container');
  quoteContainer.innerText = quote;
}

/** Display comments on the page, capped at the user's requested maximum */
function getComments(maxComments) {
  fetch('/login').then(response => response.text()).then((loginLink) => {
    console.log("link: " + loginLink);
    if (loginLink.trim() == "") {
      console.log("logged in");
      // Show comment form
      document.getElementById('comment-form').style.display = "block";
      document.getElementById('login-link').style.display = "none";
    } else {
      document.getElementById('comment-form').style.display = "none";
      const login = document.getElementById('login-link');
      login.style.display = "block";
      login.innerHTML = "Login <a href=\"" + loginLink + "\">here</a> to leave a comment!"

      // var element = document.createElement("a");
      // element.setAttribute("href", loginLink);
      // element.innerHTML = "Login here to leave a comment!";

      // and append it to where you'd like it to go:
      //document.body.appendChild(element);
    }
  });

  fetch('/data?max-comments=' + maxComments.toString()).then(response => response.json()).then((comments) => {
    console.log(comments);
    // Build the list of history entries.
    const commentElement = document.getElementById('data-container');
    commentElement.innerHTML = '';
    comments.history.forEach((line) => {
      console.log(line);
      commentElement.appendChild(createListElement(line));
    });
  });
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

/** Deletes all comments in the portfolio */
function deleteComments() {
  const request = new Request('/delete-data', {method: 'POST'});
  fetch(request).then(() => getComments(10));
}