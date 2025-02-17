EASTER_EGG_URLS

# HTML Analyzer

## Overview

The **HTML Analyzer** is a Java program that fetches and parses HTML content from a given URL, building a tree structure to analyze the deepest text content in the document.

## Features

- Fetches HTML content from a URL using Java's `HttpClient`
- Parses the HTML into a tree-like structure
- Identifies and extracts the deepest text node using a **depth-first search (DFS)** algorithm
- Handles malformed HTML by verifying proper tag nesting

## Usage
To build the program, use the following command:
```sh
javac HtmlAnalyzer
```

To run the program, use the following command:

```sh
java HtmlAnalyzer <URL>
```

Example:

```sh
java HtmlAnalyzer http://hiring.axreng.com/internship/example2.html
```

## Example Input/Output

### Sample HTML:

```html
<html>
<head>
    <title>
        This is the title in level 3.
    </title>
</head>
<body>
<div>
    This is in level 3.
</div>
<div>
    <p>
        This is in level 4. Correct result.
    </p>
    <p>
        This is also in level 4, but it is not the first result.
    </p>
</div>
This is in level 2.
<p>
    Last paragraph, level 3.
</p>
</body>
</html>
```

### Output:

```
This is in level 4. Correct result.
```

## Error Handling

- **Malformed HTML**: Displays `malformed HTML` if the tag structure is incorrect.
- **Invalid URL**: Displays `URL connection error` if fetching fails.

## Dependencies
This program relies only on standard **Java SE** libraries.

