import java.io.BufferedReader;
import java.io.StringReader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class HtmlAnalyzer {
    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                throw new RuntimeException("URL connection error");
            }
            String rawHtml = HtmlFetcher.fetch(args[0]);
            HtmlParser parsedHtml = new HtmlParser(rawHtml);
            String deepestText = parsedHtml.getDeepestText();
            System.out.println(deepestText);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private static class HtmlFetcher {
        public static String fetch(String url) throws RuntimeException {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .GET()
                        .build();

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(20))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            } catch (Exception e ) {
                throw new RuntimeException("URL connection error");
            }
        }
    }

    private static class HtmlParser{
        private TreeNode root;
        private String deepestText = null;

        public HtmlParser(String html){
           parseHtml(html);
        }

        private void parseHtml(String html){
            try (BufferedReader reader = new BufferedReader(new StringReader(html))){

                //Responsible for verifying malformed HTML
                Stack<String> htmlStack = new Stack<String>();

                //Responsible for finding deepest text
                root = new TreeNode("root", -1);

                TreeNode current = root;
                String currentLine;

                while ((currentLine = reader.readLine()) != null) {
                    currentLine = currentLine.trim();
                    String cleanedLine = currentLine.replaceAll("[<>/]", "");

                    if (!currentLine.isEmpty()) {
                        if(currentLine.startsWith("</")){
                            current = processClosingTag(cleanedLine, htmlStack, current);
                        }
                        else if(currentLine.startsWith("<")){
                            current = processOpeningTag(cleanedLine, htmlStack, current);
                        }
                        else {
                            current.text = currentLine;
                        }
                    }
                }
                if (!htmlStack.isEmpty()) {
                    throw new RuntimeException("malformed HTML");
                }
            }
            catch (Exception e) {
                throw new RuntimeException("malformed HTML");
            }
        }

        private TreeNode processOpeningTag(String tag, Stack<String> htmlStack, TreeNode current) {
            htmlStack.push(tag);
            TreeNode newNode = new TreeNode(tag, current.depth + 1);
            newNode.parent = current;
            current.children.add(newNode);
            return newNode;
        }

        private TreeNode processClosingTag(String tag, Stack<String> htmlStack, TreeNode current){
            if (htmlStack.isEmpty() || !htmlStack.peek().equals(tag)) {
                throw new RuntimeException("malformed HTML");
            }
            htmlStack.pop();
            return current.parent != null ? current.parent : current;
        }

        public String getDeepestText() {
            if (root == null) return null;
            if(deepestText == null) {
                deepestText = findDeepestText();
            }
            return deepestText;
        }

        private String findDeepestText() {
            if (root == null) return null;

            String deepestText = null;
            int maxDepth = -1;

            Stack<TreeNode> stack = new Stack<>();
            stack.push(root);

            while (!stack.isEmpty()) {
                TreeNode currentNode = stack.pop();

                if (currentNode.text != null && currentNode.depth > maxDepth) {
                    deepestText = currentNode.text;
                    maxDepth = currentNode.depth;
                }

                for (int i = currentNode.children.size() - 1; i >= 0; i--) {
                    stack.push(currentNode.children.get(i));
                }
            }

            return deepestText;
        }

    }

    private static class TreeNode {
        String tag;
        String text;
        int depth;
        List<TreeNode> children;
        TreeNode parent;

        public TreeNode(String tag, int depth) {
            this.tag = tag;
            this.depth = depth;
            this.children = new ArrayList<>();
            this.text = null;
            this.parent = null;
        }
    }

}