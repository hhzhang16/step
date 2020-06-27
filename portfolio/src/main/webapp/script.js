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
 * Adds a random greeting to the page.
 */
function addQuote() {
//   const greetings =
//       ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];
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

  // Pick a random greeting.
  const quote = quotes[Math.floor(Math.random() * quotes.length)];
  console.log(quote);

  // Add it to the page.
  const quoteContainer = document.getElementById('greeting-container');
  quoteContainer.innerText = quote;
}

function greetSelf() {
  console.log("fetching data");
  fetch('/data').then(response => response.json()).then((data) => {
    console.log(data);
    console.log(data.cs);
    const dataListElement = document.getElementById('symsys-container');
    dataListElement.innerHTML = '';
    dataListElement.appendChild(createListElement(data.cs));
    dataListElement.appendChild(createListElement(data.ling));
    dataListElement.appendChild(createListElement(data.psych));
    dataListElement.appendChild(createListElement(data.phil));
  });
}

function getComments() {
  fetch('/data').then(response => response.json()).then((comments) => {
    console.log(comments);
    // Build the list of history entries.
    const commentElement = document.getElementById('data-container');
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
