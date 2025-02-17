import java.io.BufferedReader;
import java.io.StringReader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.Queue;
import java.util.LinkedList;



public class HtmlAnalyzer {
    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                throw new RuntimeException("URL connection error");
            }
            String rawHtml = HtmlFetcher.fetch(args[0]);
            HtmlParser parsedHtml = new HtmlParser(rawHtml);
            String deep = parsedHtml.getDeepsetText();
            System.out.println(deep);
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
        private String deepsetText = null;

        public HtmlParser(String html){
            try (BufferedReader reader = new BufferedReader(new StringReader(html))){
                Stack<String> htmlStack = new Stack<String>();

                root = new TreeNode("root", -1);
                TreeNode current = root;

                String currentLine;

                while ((currentLine = reader.readLine()) != null) {
                    currentLine = currentLine.trim();
                    String cleanedLine = currentLine.replaceAll("[<>/]", "");

                    if (!currentLine.isEmpty()) {
                        if(currentLine.startsWith("</")){
                            if(!Objects.equals(htmlStack.peek(), cleanedLine)){
                                throw new RuntimeException("malformed HTML");
                            }
                            else{
                                htmlStack.pop();
                                if (current != root) {
                                    current = current.parent;
                                }
                            }
                        }
                        else if(currentLine.startsWith("<")){
                            htmlStack.push(cleanedLine);
                            TreeNode newNode = new TreeNode(cleanedLine, current.depth + 1);
                            newNode.parent = current;
                            current.children.add(newNode);
                            current = newNode;
                        }
                        else {
                            // Text content: store it in the current node
                            current.text = cleanedLine;
                        }
                    }
                }
            }
            catch (Exception e) {
                throw new RuntimeException("malformed HTML");
            }
        }

        public String getDeepsetText() {
            if (deepsetText != null) {
                return deepsetText;
            }
            if (root == null) {
                return null;
            }
            Queue<TreeNode> queue = new LinkedList<>();
            queue.add(root);
            TreeNode deepestTextNode = null;

            while (!queue.isEmpty()) {
                TreeNode node = queue.poll();
                // Check if the node contains text
                if (node.text != null && (deepestTextNode == null || node.depth > deepestTextNode.depth)) {
                    deepestTextNode = node;
                }

                // Add all children to the queue
                queue.addAll(node.children);
            }
            return deepestTextNode != null ? deepestTextNode.text : null;
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